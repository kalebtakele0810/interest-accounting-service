package et.kacha.interestcalculating.util;


import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.entity.Customers;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Subscriptions;
import et.kacha.interestcalculating.entity.Transactions;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.SubscriptionsRepository;
import et.kacha.interestcalculating.repository.TransactionsRepository;
import et.kacha.interestcalculating.scheduled_tasks.DailyInterestCalculator;
import org.junit.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DailyInterestUtilityTest {
    private DailyBalanceUtility dailyBalanceUtility;
    private final double DELTA = 0.001;

    @Before
    public void setUp() {
        dailyBalanceUtility = new DailyBalanceUtility();
    }

    @Test
    public void searchDailyProductsTest() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ArrayList<Transactions> transactions = new ArrayList<>();

        transactions.add(Transactions.builder()
                .status(TransactionStatus.SUCCESS)
                .balance(1000f)
                .transaction_type(TransactionType.WITHDRAWAL)
                .updated_at(new Timestamp(dateFormat.parse("2024-03-15 10:30:00").getTime()))
                .build());

        transactions.add(Transactions.builder()
                .status(TransactionStatus.SUCCESS)
                .balance(3000f)
                .transaction_type(TransactionType.DEPOSIT)
                .updated_at(new Timestamp(dateFormat.parse("2024-03-19 10:30:00").getTime()))
                .build());


        transactions.add(Transactions.builder()
                .status(TransactionStatus.SUCCESS)
                .balance(7000f)
                .transaction_type(TransactionType.WITHDRAWAL)
                .updated_at(new Timestamp(dateFormat.parse("2024-03-20 10:30:00").getTime()))
                .build());

        transactions.add(Transactions.builder()
                .status(TransactionStatus.SUCCESS)
                .balance(9000f)
                .transaction_type(TransactionType.WITHDRAWAL)
                .updated_at(new Timestamp(dateFormat.parse("2024-03-20 10:31:00").getTime()))
                .build());

        assertEquals(9000f, DailyBalanceUtility.calculateMinimumBalance(transactions), DELTA);

    }
}
