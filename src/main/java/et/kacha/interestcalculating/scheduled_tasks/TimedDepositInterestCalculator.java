package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
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
import java.time.Year;
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

    //    @Scheduled(cron = "0 0 0 L * *")
    @Scheduled(cron = "*/200 * * * * *", zone = "GMT+3")
    public void searchTimeDepositProducts() {

        List<Products> products = productsRepository.findByProduct_typeAndState(ProductType.TIMED, ProductState.ACTIVE);

        LocalDate currentDate = LocalDate.now();

        for (Products product : products) {

            List<Subscriptions> subscriptions = subscriptionsRepository.findByProductId(product.getId());

            for (Subscriptions subscription : subscriptions) {

                Customers customer = subscription.getCustomer();

                log.info("Calculating timed-deposit interest for customer" + customer.getPhone());

                List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerIdAndStatus(
                        product.getId(),
                        customer.getId(),
                        ProductState.ACTIVE,
                        TransactionStatus.SUCCESS);

                if (Objects.nonNull(transactionsList)) {
                    float minimumBalance = TimedDepositBalanceUtility.calculateMinimumBalance(transactionsList, product);
                    interestUtility.saveInterest(currentDate, product, subscription, minimumBalance);
                }
            }
        }
    }


}
