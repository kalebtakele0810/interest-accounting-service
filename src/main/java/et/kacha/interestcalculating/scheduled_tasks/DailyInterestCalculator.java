package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.util.DailyBalanceUtility;
import et.kacha.interestcalculating.util.InterestUtility;
import et.kacha.interestcalculating.util.MonthlyBalanceUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Objects;

//import java.util.Calendar;
//import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyInterestCalculator {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;

    private final InterestUtility interestUtility;

    //    @Scheduled(cron = "0 0 0 L * *")
//    @Scheduled(cron = "*/200 * * * * *", zone = "GMT+3")
    public void searchDailyProducts() {

        List<Products> products = productsRepository.findByInterestCompTypeAndProductstate(
                InterestCompType.DAILY,
                ProductState.ACTIVE,
                ProductType.REGULAR);

        for (Products product : products) {

            List<Subscriptions> subscriptions = subscriptionsRepository.findByProductId(product.getId());

            for (Subscriptions subscription : subscriptions) {

                Customers customer = subscription.getCustomer();

                log.info("Calculating daily regular interest for customer" + customer.getPhone());

                List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                        product.getId(),
                        customer.getId(),
                        ProductState.ACTIVE,
                        TransactionStatus.SUCCESS);

                if (Objects.nonNull(transactionsList)) {
                    float interestPayableBalance = 0;
                    if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.MIN_BALANCE)) {
                        interestPayableBalance = DailyBalanceUtility.calculateMinimumBalance(transactionsList);
                    }
                    if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.AVERAGE)) {
                        interestPayableBalance = DailyBalanceUtility.calculateAverageBalance(transactionsList);
                    }
                    interestUtility.saveDailyInterest(product, subscription, interestPayableBalance);
                }
            }

        }
    }
}
