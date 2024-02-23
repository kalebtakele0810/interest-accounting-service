package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.Customers;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.util.CalenderUtil;
import et.kacha.interestcalculating.util.InterestUtility;
import et.kacha.interestcalculating.util.MonthlyBalanceUtility;
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
public class MonthlyInterestCalculator {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;

    private final InterestUtility interestUtility;
    @Scheduled(cron = "0 30 0 * * *", zone = "GMT+3")
    public void searchMonthlyProducts() {

        log.info("Regular Monthly interest processing started.");
//        List<Products> products = productsRepository.findAll();
        List<Products> products = productsRepository.findByInterestCompTypeAndProductstate(
                InterestCompType.MONTHLY,
                ProductState.ACTIVE,
                ProductType.REGULAR);

        LocalDate currentDate = LocalDate.now();

        LocalDate lastDayOfMonth = new CalenderUtil().getLastDayOfMonth(currentDate);

        for (Products product : products) {

            if (currentDate.isEqual(lastDayOfMonth)) {

                List<Subscriptions> subscriptions = subscriptionsRepository.findByProductIdAndStatus(product.getId(), SubscriptionStatus.ACTIVE);

                for (Subscriptions subscription : subscriptions) {

                    Customers customer = subscription.getCustomer();

                    List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                            product.getId(),
                            customer.getId(),
                            ProductState.ACTIVE,
                            TransactionStatus.SUCCESS,
                            SubscriptionStatus.ACTIVE);

                    if (Objects.nonNull(transactionsList)) {
                        float interestPayableBalance = 0;
                        if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.MIN_BALANCE)) {
                            interestPayableBalance = MonthlyBalanceUtility.calculateMinimumBalance(transactionsList);
                        }
                        if (product.getInterest_calculated_using().equals(InterestCalculatedUsing.AVG_BALANCE)) {
                            interestPayableBalance = MonthlyBalanceUtility.calculateAverageBalance(transactionsList);
                        }


                        interestUtility.saveInterest(currentDate, product, subscription, interestPayableBalance);
                    }
                }
            }
        }
        log.info("Regular Monthly interest processing ended.");
    }
}
