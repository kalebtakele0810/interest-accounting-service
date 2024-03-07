package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.ProductState;
import et.kacha.interestcalculating.constants.ProductType;
import et.kacha.interestcalculating.constants.SubscriptionStatus;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.entity.Customers;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.util.InterestUtility;
import et.kacha.interestcalculating.util.TimedDepositBalanceUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimedDepositInterestCalculator {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;

    private final InterestUtility interestUtility;

    @Scheduled(cron = "0 30 23 * * *", zone = "GMT+3")
    public void searchTimeDepositProducts() {

        log.info("Timed deposit interest processing started.");

        List<Products> products = productsRepository.findByProductTypeAndState(ProductType.TIME, ProductState.ACTIVE);

        LocalDate currentDate = LocalDate.now();

        for (Products product : products) {

            List<Subscriptions> subscriptions = subscriptionsRepository.findByProductIdAndStatus(product.getId(),
                    SubscriptionStatus.ACTIVE);

            for (Subscriptions subscription : subscriptions) {

                Customers customer = subscription.getCustomer();

                List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                        product.getId(),
                        customer.getId(),
                        ProductState.ACTIVE,
                        TransactionStatus.SUCCESS,
                        SubscriptionStatus.ACTIVE);

                if (Objects.nonNull(transactionsList)) {
                    float minimumBalance = TimedDepositBalanceUtility.calculateMinimumBalance(transactionsList, product);
                    interestUtility.saveTimedInterest(currentDate, product, subscription, minimumBalance);
                }
            }
        }
        log.info("Timed deposit interest processing ended.");
    }
}
