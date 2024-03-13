package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.constants.ChargeRate;
import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.ChargeFeesRepository;
import et.kacha.interestcalculating.repository.ChargesRepository;
import et.kacha.interestcalculating.repository.InterestFeeHistoryRepository;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterestUtility {

    private final InterestHistoryRepository interestHistoryRepository;

    public void saveInterest(LocalDate currentDate, Products product, Subscriptions subscription, float balance) {
        if (balance > 0) {
            double decimalPlaces = Math.pow(10, Objects.isNull(product.getDecimal_places()) ? 2 : product.getDecimal_places());
            double interestValue = balance *
                    (product.getInterest_rate() / decimalPlaces) *
                    ((float) currentDate.lengthOfMonth() / Year.now().length());
            double baseInterest = (double) ((int) (interestValue * decimalPlaces)) / decimalPlaces;


            if (Objects.nonNull(product.getMin_interest_bearing_amt()) ? balance > product.getMin_interest_bearing_amt() : true) {
                log.info("Monthly interest saved for:" + subscription.getCustomer().getPhone() + " base amount:" + baseInterest);
                InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                        .interest_before_deduction(baseInterest)
                        .interest_rate(Double.valueOf(product.getInterest_rate()))
                        .balance((double) balance)
                        .subscriptions(subscription)
                        .status(InterestPaymentState.UNPROCESSED)
                        .build());
            }
        }
    }

    public void saveTimedInterest(LocalDate currentDate, Products product, Subscriptions subscription, float balance) {
        if (balance > 0) {
            double decimalPlaces = Math.pow(10, Objects.isNull(product.getDecimal_places()) ? 2 : product.getDecimal_places());
            double interestValue = balance *
                    (product.getInterest_rate() / decimalPlaces) *
                    ((float) (product.getTerm_duration() * 30) / Year.now().length());
            double baseInterest = (double) ((int) (interestValue * decimalPlaces)) / decimalPlaces;


            if (Objects.nonNull(product.getMin_interest_bearing_amt()) ? balance > product.getMin_interest_bearing_amt() : true) {
                log.info("Timed interest saved for:" + subscription.getCustomer().getPhone() + " base amount:" + baseInterest);
                InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                        .interest_before_deduction(baseInterest)
                        .interest_rate(Double.valueOf(product.getInterest_rate()))
                        .balance((double) balance)
                        .subscriptions(subscription)
                        .status(InterestPaymentState.UNPROCESSED)
                        .build());
            }
        }
    }

    public void saveDailyInterest(Products product, Subscriptions subscription, float balance) {
        if (balance > 0) {

            double decimalPlaces = Math.pow(10, Objects.isNull(product.getDecimal_places()) ? 2 : product.getDecimal_places());
            double interestValue = (balance *
                    (product.getInterest_rate() / decimalPlaces)) / Year.now().length();
            int interestVl = (int) (interestValue * decimalPlaces);
            double baseInterest = (double) interestVl / decimalPlaces;
            if (Objects.nonNull(product.getMin_interest_bearing_amt()) ? balance > product.getMin_interest_bearing_amt() : true) {
                log.info("Daily interest saved for:" + subscription.getCustomer().getPhone() + " base amount:" + baseInterest);
                InterestHistory interestHistory = interestHistoryRepository.save(InterestHistory.builder()
                        .interest_before_deduction(baseInterest)
                        .interest_rate(Double.valueOf(product.getInterest_rate()))
                        .balance((double) balance)
                        .subscriptions(subscription)
                        .status(InterestPaymentState.UNPROCESSED)
                        .build());
            }
        }
    }


}
