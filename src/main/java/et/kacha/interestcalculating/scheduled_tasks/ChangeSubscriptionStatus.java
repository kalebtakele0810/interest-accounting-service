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

//    @Scheduled(cron = "0 0 3 * * *", zone = "GMT+3")
    public void searchProductsForInactivity() {

        log.info("Scheduled inactivation started.");

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
                        SubscriptionStatus.ACTIVE,
                        false);
                if (!transactionsList.isEmpty()) {
                    Collections.sort(transactionsList, Comparator.comparing(Transactions::getUpdated_at));
                    Transactions lastTransaction = transactionsList.get(transactionsList.size() - 1);
                    long daysSinceLastTransaction = ChronoUnit.DAYS.between(lastTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(), LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_inactivity()) && daysSinceLastTransaction > product.getDays_for_inactivity()) {
                        log.info("subscription:" + subscription.getId() + " inactivated");
                        subscription.setStatus(SubscriptionStatus.INACTIVE);
                        subscriptionsRepository.save(subscription);
                        transactionsRepository.updateIs_archivedBySubscriptions(true, subscription);
                    }
                } else {
                    long daysSinceLastTransaction = ChronoUnit.DAYS.between(subscription.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(), LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_inactivity()) && daysSinceLastTransaction > product.getDays_for_inactivity()) {
                        log.info("subscription:" + subscription.getId() + " inactivated");
                        subscription.setStatus(SubscriptionStatus.INACTIVE);
                        subscriptionsRepository.save(subscription);
                        transactionsRepository.updateIs_archivedBySubscriptions(true, subscription);
                    }
                }

            }
        }
        log.info("Scheduled inactivation ended.");
    }

//    @Scheduled(cron = "0 0 4 * * *", zone = "GMT+3")
    public void searchProductsForDormancy() {

        log.info("Scheduled dormancy started.");

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
                        SubscriptionStatus.INACTIVE,
                        true);
                if (!transactionsList.isEmpty()) {
                    Collections.sort(transactionsList, Comparator.comparing(Transactions::getUpdated_at));
                    Transactions lastTransaction = transactionsList.get(transactionsList.size() - 1);
                    long daysSinceLastTransaction =
                            ChronoUnit.DAYS.between(lastTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(),
                                    LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_dormancy()) && daysSinceLastTransaction > product.getDays_for_dormancy()) {
                        log.info("subscription:" + subscription.getId() + " dormanted");
                        subscription.setStatus(SubscriptionStatus.DORMANT);
                        subscriptionsRepository.save(subscription);
                    }
                } else {
                    long daysSinceLastTransaction = ChronoUnit.DAYS.between(subscription.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(),
                            LocalDate.now());
                    if (Objects.nonNull(product.getDays_for_dormancy()) && daysSinceLastTransaction > product.getDays_for_dormancy()) {
                        log.info("subscription:" + subscription.getId() + " dormanted");
                        subscription.setStatus(SubscriptionStatus.DORMANT);
                        subscriptionsRepository.save(subscription);
                    }
                }

            }
        }
        log.info("Scheduled dormancy ended.");
    }
}
