package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import et.kacha.interestcalculating.util.CalenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class InterestDeductionsCalculator {

    private final InterestHistoryRepository interestHistoryRepository;

    private final ChargesRepository chargesRepository;

    private final ChargeFeesRepository chargeFeesRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    @Scheduled(cron = "0 07 0 * * *", zone = "GMT+3")
    public void searchCharges() {

        log.info("Deduction service processing started.");

//        LocalDate currentDate = LocalDate.now();
        LocalDate currentDate = LocalDate.now().minusDays(1);

        LocalDate lastDayOfMonth = new CalenderUtil().getLastDayOfMonth(currentDate);

        if (currentDate.isEqual(lastDayOfMonth)) {

            List<InterestHistory> interestHistories = interestHistoryRepository.findByCompTypeStatus(InterestPaymentState.UNPROCESSED, InterestCompType.DAILY);

            Map<Subscriptions, Double> dailyInterestsSum = interestHistories.stream()
                    .collect(Collectors.groupingBy(InterestHistory::getSubscriptions,
                            Collectors.summingDouble(InterestHistory::getInterest_before_deduction)));
            dailyInterestsSum.forEach(this::calculateDailyDeductions);

            //////////////////Process For remaining separately
            interestHistories = interestHistoryRepository.findByStatus(InterestPaymentState.UNPROCESSED);
            for (InterestHistory interestHistory : interestHistories) {

                Subscriptions subscription = interestHistory.getSubscriptions();
                double baseInterest = interestHistory.getInterest_before_deduction();
                Products product = subscription.getProduct();
                if (Objects.isNull(product.getInterest_comp_type()) || !product.getInterest_comp_type().equals(InterestCompType.DAILY)) {
                    double totalCharges = calculateCharges(product, baseInterest, interestHistory);
                    double totalTaxes = calculateTax(product, baseInterest, interestHistory);
                    double netInterest = baseInterest - totalCharges - totalTaxes;
                    interestHistory.setInterest_after_deduction(netInterest > 0 ? netInterest : 0);
                    interestHistory.setStatus(InterestPaymentState.SAVED);

                    DecimalFormat df = new DecimalFormat("#.##");
                    log.info("Deduction detail for interest history id: {} | Initial base interest: {} " +
                                    "| net interest after deduction: {} | total tax deducted: {} | total charge deducted: {}",
                            interestHistory.getId(), df.format(baseInterest), df.format(netInterest), df.format(totalTaxes), df.format(totalCharges));

                    interestHistoryRepository.save(interestHistory);
                }
            }
        }
        log.info("Deduction service processing ended.");
    }

    private void calculateDailyDeductions(Subscriptions subscription, Double interestSum) {

        Products product = subscription.getProduct();

        InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                .interest_before_deduction(interestSum)
                .interest_rate(Double.valueOf(product.getInterest_rate()))
                .balance((double) 0)
                .subscriptions(subscription)
                .status(InterestPaymentState.SAVED)
                .build());

        interestHistoryRepository.save(interestHistory);

        interestHistoryRepository.updateStatusBySubscriptionsAndStatus(InterestPaymentState.PAID, subscription, InterestPaymentState.UNPROCESSED);

        double baseInterest = interestHistory.getInterest_before_deduction();
        double totalCharges = calculateCharges(product, baseInterest, interestHistory);
        double totalTaxes = calculateTax(product, baseInterest, interestHistory);
        double netInterest = baseInterest - totalCharges - totalTaxes;
        interestHistory.setInterest_after_deduction(netInterest > 0 ? netInterest : 0);
        interestHistory.setStatus(InterestPaymentState.SAVED);

        DecimalFormat df = new DecimalFormat("#.##");
        log.info("Deduction detail for interest history id: {} | Initial base interest: {} " +
                        "| net interest after deduction: {} | total tax deducted: {} | total charge deducted: {}",
                interestHistory.getId(), df.format(baseInterest), df.format(netInterest), df.format(totalTaxes), df.format(totalCharges));

        interestHistoryRepository.save(interestHistory);


    }

    private double calculateCharges(Products product, double baseInterest, InterestHistory interestHistory) {
        double chargesSum = 0;
        List<Charge> charges = chargesRepository.findByProductsIdAndStatusAndChargeFor(product.getId(), ChargeState.ACTIVE, ChargeFor.MAF);
//        List<Charge> charges = chargesRepository.findByProductsIdAndStatus(product.getId(), ChargeState.ACTIVE);
//      List<Charge> charges = chargesRepository.findByProductsIdAndStatus(product.getId(), "Interest", ChargeState.ACTIVE, ChargeState.ACTIVE);

        for (Charge charge : charges) {
            List<ChargeFees> chargeFees = chargeFeesRepository.findByChargeIdAndStatus(charge.getId(), ChargeState.ACTIVE);

            double interestRate = 0;
            for (ChargeFees chargeFee : chargeFees) {
                if ((Objects.isNull(chargeFee.getRange_minimum()) && Objects.isNull(chargeFee.getRange_maximum())) ||
                        (chargeFee.getRange_minimum() == 0 && chargeFee.getRange_maximum() == 0) ||
                        (chargeFee.getRange_minimum() <= baseInterest && chargeFee.getRange_maximum() > baseInterest)) {
                    interestRate = chargeFee.getCharge_amount();
                }
            }
            if (interestRate > 0) {
                if (charge.getCharge_calculation_type().equals(ChargeRate.FLAT)) {
                    interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                            .amount(interestRate)
                            .charge_rate(interestRate)
                            .interestHistory(interestHistory)
                            .charge(charge)
                            .status(InterestPaymentState.SAVED)
                            .build());
                    chargesSum += interestRate;
                } else if (charge.getCharge_calculation_type().equals(ChargeRate.PERCENTAGE)) {
                    interestFeeHistoryRepository.save(InterestFeeHistory.builder()
                            .amount((baseInterest * interestRate / 100))
                            .charge_rate(interestRate)
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
                    .fee_rate(Double.valueOf(product.getTax_fee_amount()))
                    .interestHistory(interestHistory)
                    .status(InterestPaymentState.SAVED)
                    .build());
        }
        return chargesSum;
    }
}



