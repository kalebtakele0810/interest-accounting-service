package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.ChargeRate;
import et.kacha.interestcalculating.constants.ChargeState;
import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class InterestDeductionsCalculator {

    private final InterestHistoryRepository interestHistoryRepository;

    private final ChargesRepository chargesRepository;

    private final ChargeFeesRepository chargeFeesRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    @Scheduled(cron = "0 0 2 * * *", zone = "GMT+3")
   public void searchMonthlyProducts() {
        log.info("Deduction service processing started.");
        List<InterestHistory> interestHistories = interestHistoryRepository.findByStatus(InterestPaymentState.UNPROCESSED);
        for (InterestHistory interestHistory : interestHistories) {
            Subscriptions subscription = interestHistory.getSubscriptions();
            double baseInterest = interestHistory.getAmount();
            Products product = subscription.getProduct();
            double totalCharges = calculateCharges(product, baseInterest, interestHistory);
            double totalTaxes = calculateTax(product, baseInterest, interestHistory);
            double netInterest = baseInterest - totalCharges - totalTaxes;
            interestHistory.setAmount(netInterest);
            interestHistory.setStatus(InterestPaymentState.SAVED);
            interestHistoryRepository.save(interestHistory);
        }
        log.info("Deduction service processing ended.");
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



