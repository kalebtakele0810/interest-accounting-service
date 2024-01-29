package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.InterestState;
import et.kacha.interestcalculating.constants.ProductType;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.util.BalanceUtility;
import et.kacha.interestcalculating.util.CalenderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//import java.util.Calendar;
//import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterestCalculator {

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final TransactionsRepository transactionsRepository;

    private final InterestHistoryRepository interestHistoryRepository;

    //    @Scheduled(cron = "0 0 0 L * *")
    @Scheduled(cron = "*/5 * * * * *", zone = "GMT+3")
    public void searchProducts() {

        List<Products> products = productsRepository.findByStatus("active");

        Calendar currentDate = Calendar.getInstance();

        Calendar lastDayOfMonth = CalenderUtil.getLastDayOfMonth(currentDate);

        Calendar firstDayOfMonth = CalenderUtil.getFirstDayOfMonth(currentDate);

        System.out.println("Curr=" + currentDate + " Last=" + lastDayOfMonth + " First=" + firstDayOfMonth);

        for (Products product : products) {

            if (product.getDuration().equalsIgnoreCase(String.valueOf(ProductType.MONTHLY))) {

                if (currentDate.get(Calendar.DAY_OF_YEAR) == lastDayOfMonth.get(Calendar.DAY_OF_YEAR) &&
                        currentDate.get(Calendar.YEAR) == lastDayOfMonth.get(Calendar.YEAR)) {

                    List<Subscriptions> subscriptions = subscriptionsRepository.findByProductId(product.getId());

                    for (Subscriptions subscription : subscriptions) {

                        Customers customer = subscription.getCustomer();

                        log.info("Calculating interest for customer" + customer.getMsisdn());

                        List<Transactions> transactionsList = transactionsRepository.findByProductIdAndCustomerId(product.getId(), customer.getId());

                        if (Objects.nonNull(transactionsList)) {
                            int minimumBalance = BalanceUtility.calculateMinimumBalance(transactionsList, ProductType.MONTHLY, currentDate, firstDayOfMonth, lastDayOfMonth);
                            float interestValue = minimumBalance *
                                    (product.getRate() / 100) *
                                    ((float) currentDate.getActualMaximum(Calendar.DAY_OF_MONTH) / 365);
                            int interestVl = (int) (interestValue * 100);
                            interestHistoryRepository.save(InterestHistory.builder()
                                    .amount(interestVl / 100)
                                    .product(product)
                                    .customer(customer)
                                    .status(InterestState.SAVED)
                                    .build());
                        }
                    }


                }
            }

        }


    }
}
