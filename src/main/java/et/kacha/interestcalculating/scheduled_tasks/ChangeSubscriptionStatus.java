package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.ProductState;
import et.kacha.interestcalculating.constants.SubscriptionStatus;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.entity.Customers;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangeSubscriptionStatus {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;
    @Scheduled(cron =  "0 0 3 * * *", zone = "GMT+3")
    public void searchTimeDepositProducts() {

        log.info("Scheduled deactivation started.");

        List<Products> products = productsRepository.findByState(ProductState.ACTIVE);

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
                if (transactionsList.size() > 0) {
                    Collections.sort(transactionsList, Comparator.comparing(Transactions::getUpdated_at));
                    Transactions lastTransaction = transactionsList.get(transactionsList.size() - 1);
                    long DaysSinceLastTransaction = ChronoUnit.DAYS.between(lastTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(), LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_inactivity()) && DaysSinceLastTransaction > product.getDays_for_inactivity()) {
                        log.info("subscription:" + subscription.getId() + " deactivated");
                        subscription.setStatus(SubscriptionStatus.INACTIVE);
                        subscriptionsRepository.save(subscription);
                    }
                } else {
                    long DaysSinceLastTransaction = ChronoUnit.DAYS.between(subscription.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(), LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_inactivity()) && DaysSinceLastTransaction > product.getDays_for_inactivity()) {
                        log.info("subscription:" + subscription.getId() + " deactivated");
                        subscription.setStatus(SubscriptionStatus.INACTIVE);
                        subscriptionsRepository.save(subscription);
                    }
                }

            }
        }
        log.info("Scheduled deactivation ended.");
    }
}
