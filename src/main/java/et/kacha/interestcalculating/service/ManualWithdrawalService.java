package et.kacha.interestcalculating.service;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.dto.InterestRequest;
import et.kacha.interestcalculating.dto.InterestResponse;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public InterestResponse calculateUnpaidInterest(InterestRequest interestRequest) {

        Optional<Subscriptions> subscriptions = subscriptionsRepository.findByIdAndStatus(Integer.parseInt(interestRequest.getSubscriptionId()), SubscriptionStatus.ACTIVE).stream().findAny();

        if (subscriptions.isEmpty()) {
            return InterestResponse.builder()
                    .msisdn(interestRequest.getMsisdn())
                    .interestAmount(0)
                    .chargeAmount(0)
                    .taxAmount(0)
                    .build();
        }
        Subscriptions subscription = subscriptions.get();
        Products product = subscription.getProduct();
        if (product.getInterest_comp_type().equals(InterestCompType.MONTHLY)) {
            return InterestResponse.builder()
                    .msisdn(interestRequest.getMsisdn())
                    .interestAmount(0)
                    .chargeAmount(0)
                    .taxAmount(0)
                    .build();
        }
        if (product.getInterest_comp_type().equals(InterestCompType.DAILY)) {
            return calculateDailyInterest(subscription);
        }
        if (product.getProduct_type().equals(ProductType.TIME)) {
            return calculateTimedInterest(subscription);
        }

        return InterestResponse.builder()
                .msisdn(interestRequest.getMsisdn())
                .interestAmount(0)
                .chargeAmount(0)
                .taxAmount(0)
                .build();
    }

    public InterestResponse calculateDailyInterest(Subscriptions subscription) {
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

        return InterestResponse.builder()
                .msisdn(subscription.getPhone())
                .interestAmount(netInterest)
                .chargeAmount(netCharge)
                .taxAmount(netTax)
                .build();
    }


    public InterestResponse calculateTimedInterest(Subscriptions subscription) {
        double netInterest = 0, netTax = 0, netCharge = 0;

        Customers customer = subscription.getCustomer();

        List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                subscription.getProduct().getId(),
                customer.getId(),
                ProductState.ACTIVE,
                TransactionStatus.SUCCESS,
                SubscriptionStatus.ACTIVE);

        Transactions firstTransaction = getFirstBalance(transactionsList);
        if (Objects.isNull(firstTransaction)) {
            return InterestResponse.builder()
                    .msisdn(subscription.getPhone())
                    .interestAmount(netInterest)
                    .chargeAmount(netCharge)
                    .taxAmount(netTax)
                    .build();
        } else {

            LocalDate transactionDate = firstTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
            LocalDate dueDate = transactionDate.plusMonths(subscription.getProduct().getTerm_duration());

            if (dueDate.isBefore(LocalDate.now())) {
                ///Calculate remaining interest
                return InterestResponse.builder()
                        .msisdn(subscription.getPhone())
                        .interestAmount(netInterest)
                        .chargeAmount(netCharge)
                        .taxAmount(netTax)
                        .build();
            }
            if (dueDate.isAfter(LocalDate.now())) {
                Optional<Products> ordinaryProduct = productsRepository.findByFinancial_institution_idAndIsOrdinaryAndState
                        (subscription.getProduct().getFinancial_institution_id(), true, ProductState.ACTIVE).stream().findFirst();
                if (ordinaryProduct.isEmpty()) {
                    return InterestResponse.builder()
                            .msisdn(subscription.getPhone())
                            .interestAmount(netInterest)
                            .chargeAmount(netCharge)
                            .taxAmount(netTax)
                            .build();
                } else {
                    float minimumBalance = firstTransaction.getBalance();
                    for (Transactions transaction : transactionsList) {
                        if (minimumBalance > transaction.getBalance()) {
                            return InterestResponse.builder()
                                    .msisdn(subscription.getPhone())
                                    .interestAmount(netInterest)
                                    .chargeAmount(netCharge)
                                    .taxAmount(netTax)
                                    .build();
                        }
                    }

                    if (minimumBalance > 0) {
                        double decimalPlaces = Math.pow(10, Objects.isNull(ordinaryProduct.get().getDecimal_places()) ? 2 : ordinaryProduct.get().getDecimal_places());

                        double interestValue = minimumBalance *
                                (ordinaryProduct.get().getInterest_rate() / decimalPlaces) *
                                ((float) ChronoUnit.DAYS.between(transactionDate, LocalDate.now()) / Year.now().length());
                        double baseInterest = (double) ((int) (interestValue * decimalPlaces)) / decimalPlaces;

                        if (Objects.nonNull(ordinaryProduct.get().getMin_interest_bearing_amt()) ? minimumBalance > ordinaryProduct.get().getMin_interest_bearing_amt() : true) {

                            InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                                    .amount(baseInterest)
                                    .balance(minimumBalance)
                                    .subscriptions(subscription)
                                    .status(InterestPaymentState.PAID)
                                    .build());
                            netCharge = calculateCharges(ordinaryProduct.get(), baseInterest, interestHistory);
                            netTax = calculateTax(ordinaryProduct.get(), baseInterest, interestHistory);
                            netInterest = baseInterest - netCharge - netTax;
                            interestHistory.setAmount(netInterest);

                            interestHistoryRepository.save(interestHistory);
                            return InterestResponse.builder()
                                    .msisdn(subscription.getPhone())
                                    .interestAmount(netInterest)
                                    .chargeAmount(netCharge)
                                    .taxAmount(netTax)
                                    .build();
                        }
                    }


                }

            }
        }
        return InterestResponse.builder()
                .msisdn(subscription.getPhone())
                .interestAmount(netInterest)
                .chargeAmount(netCharge)
                .taxAmount(netTax)
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
                            .amount((float) interestRate)
                            .interestHistory(interestHistory)
                            .charge(charge)
                            .status(InterestPaymentState.SAVED)
                            .build());
                    chargesSum += interestRate;
                } else if (charge.getCharge_calculation_type().equals(ChargeRate.PERCENTAGE)) {
                    interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                            .amount((float) (baseInterest * interestRate / 100))
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
                    .amount((float) chargesSum)
                    .interestHistory(interestHistory)
                    .status(InterestPaymentState.SAVED)
                    .build());
        }
        return chargesSum;
    }
}
