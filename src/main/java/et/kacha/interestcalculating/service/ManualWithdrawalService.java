package et.kacha.interestcalculating.service;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.dto.InterestRequest;
import et.kacha.interestcalculating.dto.InterestBody;
import et.kacha.interestcalculating.dto.MainRequest;
import et.kacha.interestcalculating.dto.MainResponse;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    public MainResponse calculateUnpaidInterest(InterestRequest interestRequest, MainRequest interestBody) {

        Optional<Subscriptions> subscriptions = subscriptionsRepository.findByIdAndStatus(Integer.parseInt(interestRequest.getSubscriptionId()), SubscriptionStatus.ACTIVE).stream().findAny();

        if (subscriptions.isEmpty()) {
            return MainResponse.builder()
                    .id(interestBody.getId())
                    .responseDesc("Active subscription can't be found.")
                    .responseCode("1")
                    .payload(InterestBody.builder()
                            .phone(interestRequest.getMsisdn())
                            .interestAmount(0)
                            .chargeAmount(0)
                            .taxAmount(0)
                            .build())
                    .build();
        }
        Subscriptions subscription = subscriptions.get();
        Products product = subscription.getProduct();
        if (product.getProduct_type().equals(ProductType.TIME)) {
            log.info("================" + 1 + "================");
            return MainResponse.builder()
                    .id(interestBody.getId())
                    .responseDesc("Successfully processed!")
                    .responseCode("0")
                    .payload(calculateTimedInterest(subscription))
                    .build();
        }
        if (product.getInterest_comp_type().equals(InterestCompType.MONTHLY)) {

            return MainResponse.builder()
                    .id(interestBody.getId())
                    .responseDesc("Successfully processed!")
                    .responseCode("0")
                    .payload(InterestBody.builder()
                            .phone(interestRequest.getMsisdn())
                            .interestAmount(0)
                            .chargeAmount(0)
                            .taxAmount(0)
                            .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                            .txnRef(String.valueOf(UUID.randomUUID()))
                            .subscriptionId(String.valueOf(subscription.getId()))
                            .build())
                    .build();
        }
        if (product.getInterest_comp_type().equals(InterestCompType.DAILY)) {
            return MainResponse.builder()
                    .id(interestBody.getId())
                    .responseDesc("Successfully processed!")
                    .responseCode("0")
                    .payload(calculateDailyInterest(subscription))
                    .build();
        }
        return MainResponse.builder()
                .id(interestBody.getId())
                .responseDesc("Product type not recognized.")
                .responseCode("2")
                .payload(InterestBody.builder()
                        .phone(interestRequest.getMsisdn())
                        .interestAmount(0)
                        .chargeAmount(0)
                        .taxAmount(0)
                        .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                        .txnRef(String.valueOf(UUID.randomUUID()))
                        .subscriptionId(String.valueOf(subscription.getId()))
                        .build())
                .build();
    }

    public InterestBody calculateDailyInterest(Subscriptions subscription) {
        double netInterest = 0, netTax = 0, netCharge = 0;

        List<InterestHistory> scheduledInterests = interestHistoryRepository.findBySubscriptionsIdAndStatus(subscription.getId(), InterestPaymentState.SAVED);

        for (InterestHistory interestHistory : scheduledInterests) {

            netInterest += interestHistory.getAmount();
            List<InterestFeeHistory> scheduledFees = interestFeeHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

            for (InterestFeeHistory interestFeeHistory : scheduledFees) {
                netCharge += interestFeeHistory.getAmount();
                interestFeeHistory.setStatus(InterestPaymentState.PAID);
                interestFeeHistoryRepository.save(interestFeeHistory);
            }

            List<TaxHistory> scheduledTaxes = interestTaxHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

            for (TaxHistory taxHistory : scheduledTaxes) {
                netTax += taxHistory.getAmount();
                taxHistory.setStatus(InterestPaymentState.PAID);
                interestTaxHistoryRepository.save(taxHistory);
            }

            interestHistory.setStatus(InterestPaymentState.PAID);
            interestHistoryRepository.save(interestHistory);
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


    public InterestBody calculateTimedInterest(Subscriptions subscription) {

        double netInterest = 0, netTax = 0, netCharge = 0;

        Customers customer = subscription.getCustomer();

        List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                subscription.getProduct().getId(),
                customer.getId(),
                ProductState.ACTIVE,
                TransactionStatus.SUCCESS,
                SubscriptionStatus.ACTIVE);
        log.info("================" + 2 + "================");
        Transactions firstTransaction = getFirstBalance(transactionsList);
        log.info("================" + 3 + "================");
        if (Objects.isNull(firstTransaction)) {
            log.info("================" + 4 + "================");
            return InterestBody.builder()
                    .phone(subscription.getPhone())
                    .interestAmount(netInterest)
                    .chargeAmount(netCharge)
                    .taxAmount(netTax)
                    .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                    .txnRef(String.valueOf(UUID.randomUUID()))
                    .subscriptionId(String.valueOf(subscription.getId()))
                    .build();
        } else {

            LocalDate transactionDate = firstTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
            LocalDate dueDate = transactionDate.plusDays(subscription.getProduct().getTerm_duration());
            log.info("================" + 4 + "================");
            if (dueDate.isBefore(LocalDate.now())) {
                log.info("================" + 5 + "================");
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
              /*  return InterestBody.builder()
                        .msisdn(subscription.getPhone())
                        .interestAmount(netInterest)
                        .chargeAmount(netCharge)
                        .taxAmount(netTax)
                        .build();*/
                log.info("================" + 5 + "================");
                Optional<Products> ordinaryProduct = productsRepository.findByFinancial_institution_idAndIsOrdinaryAndState
                        (subscription.getProduct().getFinancial_institution_id(), true, ProductState.ACTIVE).stream().findFirst();
                log.info("================" + 6 + "================");
                if (ordinaryProduct.isEmpty()) {
                    log.info("================" + 7 + "================");
                    return InterestBody.builder()
                            .phone(subscription.getPhone())
                            .interestAmount(netInterest)
                            .chargeAmount(netCharge)
                            .taxAmount(netTax)
                            .fiId(String.valueOf(subscription.getProduct().getFinancial_institution_id()))
                            .txnRef(String.valueOf(UUID.randomUUID()))
                            .subscriptionId(String.valueOf(subscription.getId()))
                            .build();
                } else {
                    log.info("================" + 8 + "================");
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
                    log.info("================" + 9 + "================");
                    if (minimumBalance > 0) {
                        double decimalPlaces = Math.pow(10, Objects.isNull(ordinaryProduct.get().getDecimal_places()) ? 2 : ordinaryProduct.get().getDecimal_places());

                        double interestValue = minimumBalance *
                                (ordinaryProduct.get().getInterest_rate() / decimalPlaces) *
                                ((float) ChronoUnit.DAYS.between(transactionDate, LocalDate.now()) / Year.now().length());
                        double baseInterest = (double) ((int) (interestValue * decimalPlaces)) / decimalPlaces;

                        log.info("================" + 10 + "================");
                        if (Objects.nonNull(ordinaryProduct.get().getMin_interest_bearing_amt()) ?
                                minimumBalance > ordinaryProduct.get().getMin_interest_bearing_amt() : true) {
                            log.info("================" + 11 + "================");

                           /* DecimalFormat df = new DecimalFormat("#.##");
                            String baseInterestStr = df.format(baseInterest);
                            String minimumBalanceStr = df.format(minimumBalance);

                            InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                                    .amount(Double.parseDouble(baseInterestStr))
                                    .balance(Double.parseDouble(minimumBalanceStr))
                                    .subscriptions(subscription)
                                    .status(InterestPaymentState.PAID)
                                    .build());

                            log.info("================" + 12 + "================");
                            netCharge = calculateCharges(ordinaryProduct.get(), baseInterest, interestHistory);
                            netTax = calculateTax(ordinaryProduct.get(), baseInterest, interestHistory);
                            netInterest = baseInterest - netCharge - netTax;
                            interestHistory.setAmount(netInterest);
                            if (netInterest > 0) {
                                interestHistoryRepository.save(interestHistory);
                            }*/
                            log.info("================" + 13 + "================");
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

    private double calculateCharges(Products product, double baseInterest, InterestHistory interestHistory) {
        double chargesSum = 0;
        List<Charge> charges = chargesRepository.findByProductsIdAndStatus(product.getId(), ChargeState.ACTIVE);
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
                    interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                            .amount(interestRate)
                            .interestHistory(interestHistory)
                            .charge(charge)
                            .status(InterestPaymentState.SAVED)
                            .build());
                    chargesSum += interestRate;
                } else if (charge.getCharge_calculation_type().equals(ChargeRate.PERCENTAGE)) {
                    interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                            .amount((baseInterest * interestRate / 100))
                            .interestHistory(interestHistory)
                            .charge(charge)
                            .status(InterestPaymentState.SAVED)
                            .build());
                    chargesSum += (baseInterest * interestRate / 100);
                } else {
                }
            }
        }
        return chargesSum;
    }

    private double calculateTax(Products product, double baseInterest, InterestHistory interestHistory) {

        double chargesSum = 0;

        if (product.getIs_tax_available()) {
            if (product.getTax_fee_type().equals(ChargeRate.FLAT)) {
                chargesSum = product.getTax_fee_amount();
            } else if (product.getTax_fee_type().equals(ChargeRate.PERCENTAGE)) {
                chargesSum = (baseInterest * product.getTax_fee_amount() / 100);
            }
        }
        if (chargesSum > 0) {
            interestTaxHistoryRepository.save(TaxHistory.builder()
                    .amount(chargesSum)
                    .interestHistory(interestHistory)
                    .status(InterestPaymentState.SAVED)
                    .build());
        }
        return chargesSum;
    }
}
