package et.kacha.interestcalculating.service;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.dto.*;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManualWithdrawalService {

    private final SubscriptionsRepository subscriptionsRepository;

    private final InterestHistoryRepository interestHistoryRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    private final TransactionsRepository transactionsRepository;

    private final ProductsRepository productsRepository;

    private final ChargesRepository chargesRepository;

    private final ChargeFeesRepository chargeFeesRepository;

    @Value("${interest.callback.url}")
    private String interestCallbackUrl;

    public MainInterestRequest calculateUnpaidInterest(InterestRequest interestRequest, MainRequest interestBody, boolean isProcess) {

        Optional<Subscriptions> subscriptions = subscriptionsRepository.findByIdAndStatus(Integer.parseInt(interestRequest.getSubscriptionId()), SubscriptionStatus.ACTIVE).stream().findAny();
        if (subscriptions.isEmpty()) {
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
//                    .fi_id(z.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                    .callbackUrl(interestCallbackUrl)
                    .payload(Arrays.asList(InterestBody.builder()
                            .subscriptionId(String.valueOf(interestRequest.getSubscriptionId()))
                            .interestAmount(0)
                            .taxAmount(0)
                            .chargeAmount(0)
                            .txnRef(null)
                            .phone(null)
                            .build()))
                    .build();
        }
        Subscriptions subscription = subscriptions.get();
        Products product = subscription.getProduct();
        if (product.getProduct_type().equals(ProductType.TIME)) {
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id(String.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                    .callbackUrl(interestCallbackUrl)
                    .payload(Arrays.asList(calculateTimedInterest(subscription, isProcess)))
                    .build();

        }
        if (product.getInterest_comp_type().equals(InterestCompType.MONTHLY)) {
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id(String.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                    .callbackUrl(interestCallbackUrl)
                    .payload(Arrays.asList(InterestBody.builder()
                            .subscriptionId(String.valueOf(interestRequest.getSubscriptionId()))
                            .interestAmount(0)
                            .taxAmount(0)
                            .chargeAmount(0)
                            .txnRef(null)
                            .phone(null)
                            .build()))
                    .build();
        }
        if (product.getInterest_comp_type().equals(InterestCompType.DAILY)) {

           /* return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id(String.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                    .callbackUrl(interestCallbackUrl)
                    .payload(Arrays.asList(calculateDailyInterest(subscription, isProcess)))
                    .build();*/
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id(String.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                    .callbackUrl(interestCallbackUrl)
                    .payload(Arrays.asList(InterestBody.builder()
                            .subscriptionId(String.valueOf(interestRequest.getSubscriptionId()))
                            .interestAmount(0)
                            .taxAmount(0)
                            .chargeAmount(0)
                            .txnRef(null)
                            .phone(null)
                            .build()))
                    .build();
        }
        return MainInterestRequest.builder()
                .commandId("PayInterest")
                .fi_id(String.valueOf(subscriptions.get().getProduct().getFinancial_institution_id()))
                .callbackUrl(interestCallbackUrl)
                .payload(Arrays.asList(InterestBody.builder()
                        .subscriptionId(String.valueOf(interestRequest.getSubscriptionId()))
                        .interestAmount(0)
                        .taxAmount(0)
                        .chargeAmount(0)
                        .txnRef(null)
                        .phone(null)
                        .build()))
                .build();
    }

    public InterestBody calculateDailyInterest(Subscriptions subscription, boolean isProcess) {
        double netInterest = 0, netTax = 0, netCharge = 0;

        List<InterestHistory> scheduledInterests = interestHistoryRepository.findBySubscriptionsIdAndStatus(subscription.getId(), InterestPaymentState.SAVED);

        for (InterestHistory interestHistory : scheduledInterests) {

            netInterest += interestHistory.getInterest_after_deduction();
            List<InterestFeeHistory> scheduledFees = interestFeeHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

            for (InterestFeeHistory interestFeeHistory : scheduledFees) {
                netCharge += interestFeeHistory.getAmount();
                if (isProcess) {
                    interestFeeHistory.setStatus(InterestPaymentState.PAID);
                    interestFeeHistoryRepository.save(interestFeeHistory);
                }
            }

            List<TaxHistory> scheduledTaxes = interestTaxHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

            for (TaxHistory taxHistory : scheduledTaxes) {
                netTax += taxHistory.getAmount();
                if (isProcess) {
                    taxHistory.setStatus(InterestPaymentState.PAID);
                    interestTaxHistoryRepository.save(taxHistory);
                }
            }
            if (isProcess) {
                interestHistory.setStatus(InterestPaymentState.PAID);
                interestHistoryRepository.save(interestHistory);
            }
        }
        return InterestBody.builder()
                .phone(subscription.getPhone())
                .interestAmount(netInterest)
                .chargeAmount(netCharge)
                .taxAmount(netTax)
                .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                .txnRef(String.valueOf(UUID.randomUUID()))
                .subscriptionId(String.valueOf(subscription.getId()))
                .build();
    }


    public InterestBody calculateTimedInterest(Subscriptions subscription, boolean isProcess) {

        double netInterest = 0, netTax = 0, netCharge = 0;

        Customers customer = subscription.getCustomer();

        List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                subscription.getProduct().getId(),
                customer.getId(),
                ProductState.ACTIVE,
                TransactionStatus.SUCCESS,
                SubscriptionStatus.ACTIVE,
                false);
        transactionsList = transactionsList.stream().filter(l -> l.getUpdated_at().after(subscription.getUpdated_at())).collect(Collectors.toList());
        Transactions firstTransaction = getFirstBalance(transactionsList);
        if (Objects.isNull(firstTransaction)) {
            return InterestBody.builder()
                    .phone(subscription.getPhone())
                    .interestAmount(netInterest)
                    .chargeAmount(netCharge)
                    .taxAmount(netTax)
                    .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                    .txnRef(String.valueOf(UUID.randomUUID()))
                    .subscriptionId(String.valueOf(subscription.getId()))
                    .build();
        }
        else {
            LocalDate transactionDate = firstTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
            LocalDate dueDate = transactionDate.plusDays(subscription.getProduct().getTerm_duration());
            if (dueDate.isBefore(LocalDate.now())) {
                ///Calculate remaining interest
                return InterestBody.builder()
                        .phone(subscription.getPhone())
                        .interestAmount(netInterest)
                        .chargeAmount(netCharge)
                        .taxAmount(netTax)
                        .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                        .txnRef(String.valueOf(UUID.randomUUID()))
                        .subscriptionId(String.valueOf(subscription.getId()))
                        .build();
            }
            if (dueDate.isAfter(LocalDate.now())) {
                Optional<Products> ordinaryProduct = productsRepository.findByFinancial_institution_idAndIsOrdinaryAndState
                        (subscription.getProduct().getFinancial_institution_id(), true, ProductState.ACTIVE).stream().findFirst();
                if (ordinaryProduct.isEmpty()) {
                    return InterestBody.builder()
                            .phone(subscription.getPhone())
                            .interestAmount(netInterest)
                            .chargeAmount(netCharge)
                            .taxAmount(netTax)
                            .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                            .txnRef(String.valueOf(UUID.randomUUID()))
                            .subscriptionId(String.valueOf(subscription.getId()))
                            .build();
                }
                else {
                    float minimumBalance = firstTransaction.getBalance();
                    for (Transactions transaction : transactionsList) {
                        if (minimumBalance > transaction.getBalance()) {
                            return InterestBody.builder()
                                    .phone(subscription.getPhone())
                                    .interestAmount(netInterest)
                                    .chargeAmount(netCharge)
                                    .taxAmount(netTax)
                                    .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                                    .txnRef(String.valueOf(UUID.randomUUID()))
                                    .subscriptionId(String.valueOf(subscription.getId()))
                                    .build();
                        }
                    }
                    if (minimumBalance > 0) {
                        double decimalPlaces = Math.pow(10, Objects.isNull(ordinaryProduct.get().getDecimal_places()) ? 2 : ordinaryProduct.get().getDecimal_places());
                        double interestValue = minimumBalance *
                                (ordinaryProduct.get().getInterest_rate() / 100) *
                                ((float) ChronoUnit.DAYS.between(transactionDate, LocalDate.now()) / Year.now().length());
                        double baseInterest = (double) ((int) (interestValue * decimalPlaces)) / decimalPlaces;

                        if (Objects.nonNull(ordinaryProduct.get().getMin_interest_bearing_amt()) ?
                                minimumBalance > ordinaryProduct.get().getMin_interest_bearing_amt() : true) {

                            DecimalFormat df = new DecimalFormat("#.##");
                            String baseInterestStr = df.format(baseInterest);
                            String minimumBalanceStr = df.format(minimumBalance);
                            InterestHistory interestHistory = null;
                            if (isProcess) {
                                interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                                        .interest_before_deduction(Double.parseDouble(baseInterestStr))
                                        .interest_rate(Double.valueOf(ordinaryProduct.get().getInterest_rate()))
                                        .balance(Double.parseDouble(minimumBalanceStr))
                                        .subscriptions(subscription)
                                        .status(InterestPaymentState.PAID)
                                        .build());
                            }
                            netCharge = calculateCharges(ordinaryProduct.get(), baseInterest, interestHistory, isProcess);
                            netTax = calculateTax(ordinaryProduct.get(), baseInterest, interestHistory, isProcess);
                            netInterest = baseInterest - netCharge - netTax;
                            if (isProcess) {
                                interestHistory.setInterest_after_deduction(netInterest);
                                if (netInterest > 0) {
                                    interestHistoryRepository.save(interestHistory);
                                }
                            }
                            String netInterestStr = df.format(netInterest);
                            String netChargeStr = df.format(netCharge);
                            String netTaxStr = df.format(netTax);

                            return InterestBody.builder()
                                    .phone(subscription.getPhone())
//                                    .interestAmount(Double.parseDouble(netInterestStr))
                                    .interestAmount(Double.parseDouble(baseInterestStr))
                                    .chargeAmount(Double.parseDouble(netChargeStr))
                                    .taxAmount(Double.parseDouble(netTaxStr))
                                    .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                                    .txnRef(String.valueOf(UUID.randomUUID()))
                                    .subscriptionId(String.valueOf(subscription.getId()))
                                    .build();
                        }
                    }


                }

            }
        }
        return InterestBody.builder()
                .phone(subscription.getPhone())
                .interestAmount(netInterest)
                .chargeAmount(netCharge)
                .taxAmount(netTax)
                .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                .txnRef(String.valueOf(UUID.randomUUID()))
                .subscriptionId(String.valueOf(subscription.getId()))
                .build();
    }

    private static Transactions getFirstBalance(List<Transactions> transactionsList) {

        if (transactionsList.isEmpty()) {
            return null;
        } else {
            Collections.sort(transactionsList, Comparator.comparing(Transactions::getUpdated_at));
            return transactionsList.get(0);
        }
    }

    private double calculateCharges(Products product, double baseInterest, InterestHistory interestHistory, boolean isProcess) {
        double chargesSum = 0;
        List<Charge> charges = chargesRepository.findByProductsIdAndStatus(product.getId(), ChargeState.ACTIVE);
//        List<Charge> charges = chargesRepository.findByProductsIdAndStatus(product.getId(), "Interest", ChargeState.ACTIVE, ChargeState.ACTIVE);
        for (Charge charge : charges) {
            List<ChargeFees> chargeFees = chargeFeesRepository.findByChargeIdAndStatus(charge.getId(), ChargeState.ACTIVE);
            double interestRate = 0;
            for (ChargeFees chargeFee : chargeFees) {
                if (Objects.isNull(chargeFee.getRange_minimum()) || (chargeFee.getRange_minimum() <= baseInterest && chargeFee.getRange_maximum() > baseInterest)
                ) {
                    interestRate = chargeFee.getCharge_amount();
                }
            }
            if (interestRate > 0) {
                if (charge.getCharge_calculation_type().equals(ChargeRate.FLAT)) {
                    if (isProcess) {
                        interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                                .amount(interestRate)
                                .charge_rate(interestRate)
                                .interestHistory(interestHistory)
                                .charge(charge)
                                .status(InterestPaymentState.PAID)
                                .build());
                    }
                    chargesSum += interestRate;
                } else if (charge.getCharge_calculation_type().equals(ChargeRate.PERCENTAGE)) {
                    if (isProcess) {
                        interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                                .amount((baseInterest * interestRate / 100))
                                .charge_rate(interestRate)
                                .interestHistory(interestHistory)
                                .charge(charge)
                                .status(InterestPaymentState.PAID)
                                .build());
                    }
                    chargesSum += (baseInterest * interestRate / 100);
                } else {
                }
            }
        }
        return chargesSum;
    }

    private double calculateTax(Products product, double baseInterest, InterestHistory interestHistory, boolean isProcess) {

        double chargesSum = 0;

        if (product.getIs_tax_available()) {
            if (product.getTax_fee_type().equals(ChargeRate.FLAT)) {
                chargesSum = product.getTax_fee_amount();
            } else if (product.getTax_fee_type().equals(ChargeRate.PERCENTAGE)) {
                chargesSum = (baseInterest * product.getTax_fee_amount() / 100);
            }
        }
        if (chargesSum > 0) {
            if (isProcess) {
                interestTaxHistoryRepository.save(TaxHistory.builder()
                        .amount(chargesSum)
                        .fee_rate(chargesSum)
                        .interestHistory(interestHistory)
                        .status(InterestPaymentState.SAVED)
                        .build());
            }
        }
        return chargesSum;
    }
}
