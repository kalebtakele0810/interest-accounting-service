package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.Customers;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.util.DailyBalanceUtility;
import et.kacha.interestcalculating.util.InterestUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Date;
import java.text.SimpleDateFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyInterestCalculator {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;

    private final InterestUtility interestUtility;

    @Scheduled(cron = "0 05 0 * * *", zone = "GMT+3")
    public void searchDailyProducts() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date specificDate = new Date();
        try {
            specificDate = dateFormat.parse("2024-07-28");
        } catch (Exception ignored) {

        }
        Date currentDate = new Date();
        if (currentDate.compareTo(specificDate) <= 0) {

            log.info("Regular Daily interest processing started.");
            List<Products> products = productsRepository.findByInterestCompTypeAndProductstate(
                    InterestCompType.DAILY,
                    ProductState.ACTIVE,
                    ProductType.REGULAR);

            for (Products product : products) {

                List<Subscriptions> subscriptions = subscriptionsRepository.findByProductIdAndStatus(product.getId(), SubscriptionStatus.ACTIVE);

                for (Subscriptions subscription : subscriptions) {
                    Customers customer = subscription.getCustomer();

                    List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                            product.getId(),
                            customer.getId(),
                            ProductState.ACTIVE,
                            TransactionStatus.SUCCESS,
                            SubscriptionStatus.ACTIVE,
                            false);

                    if (Objects.nonNull(transactionsList)) {
                        float interestPayableBalance = 0;
                        if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.MIN_BALANCE)) {
                            interestPayableBalance = DailyBalanceUtility.calculateMinimumBalance(transactionsList);
                        }
                        if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.AVG_BALANCE)) {
                            interestPayableBalance = DailyBalanceUtility.calculateAverageBalance(transactionsList);
                        }
                        interestUtility.saveDailyInterest(product, subscription, interestPayableBalance);
                    }

                }
            }
            log.info("Regular Daily interest processing ended.");
        }
    }
}
