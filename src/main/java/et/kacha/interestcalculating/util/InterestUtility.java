package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.entity.InterestHistory;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterestUtility {



    private final InterestHistoryRepository interestHistoryRepository;
    public void saveInterest(LocalDate currentDate, Products product, Subscriptions subscription, float balance) {
        if (balance > 0) {
            double decimalPlaces = Math.pow(10, product.getDecimal_places());
            double interestValue = balance *
                    (product.getAnnum_interest_rate() / decimalPlaces) *
                    ((float) currentDate.lengthOfMonth() / Year.now().length());
            int interestVl = (int) (interestValue * decimalPlaces);
            interestHistoryRepository.save(InterestHistory.builder()
                    .amount((double) interestVl / decimalPlaces)
                    .subscriptions(subscription)
                    .status(InterestPaymentState.SAVED)
                    .build());
        }
    }
    public void saveDailyInterest( Products product, Subscriptions subscription, float balance) {
        if (balance > 0) {
            double decimalPlaces = Math.pow(10, product.getDecimal_places());
            double interestValue = (balance *
                    (product.getAnnum_interest_rate() / decimalPlaces)) / Year.now().length();
            int interestVl = (int) (interestValue * decimalPlaces);
            interestHistoryRepository.save(InterestHistory.builder()
                    .amount((double) interestVl / decimalPlaces)
                    .subscriptions(subscription)
                    .status(InterestPaymentState.SAVED)
                    .build());
        }
    }
}
