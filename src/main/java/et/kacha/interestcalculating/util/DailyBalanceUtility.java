package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.entity.Transactions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class DailyBalanceUtility {
    public static float calculateMinimumBalance(List<Transactions> transactionsList) {

        Transactions firstTransaction = getFirstBalance(transactionsList);

        if (Objects.isNull(firstTransaction)) {
            return 0;
        } else {
            float minimiumBalance = 0;
            minimiumBalance = firstTransaction.getBalance();

            LocalDateTime yesterdayMidNight = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
            LocalDateTime todayMidNight = LocalDateTime.now().toLocalDate().atStartOfDay();

            List<Transactions> newTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                    yesterdayMidNight.isBefore(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) &&
                            todayMidNight.isAfter(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
            ).toList());
            for (Transactions transaction : newTransactions) {
                if (minimiumBalance > transaction.getBalance()) {
                    minimiumBalance = transaction.getBalance();
                }
            }
            return minimiumBalance;
        }
    }

    public static float calculateAverageBalance(List<Transactions> transactionsList) {

        Transactions firstTransaction = getFirstBalance(transactionsList);

        if (Objects.isNull(firstTransaction)) {
            return 0;
        } else {
            float averageBalance = firstTransaction.getBalance();

            LocalDateTime yesterdayMidNight = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
            LocalDateTime todayMidNight = LocalDateTime.now().toLocalDate().atStartOfDay();

            List<Transactions> newTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                    yesterdayMidNight.isBefore(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) &&
                            todayMidNight.isAfter(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())).toList());

            int index = 1;
            for (Transactions transaction : newTransactions) {
                averageBalance = (averageBalance + transaction.getBalance()) / ++index;
            }
            return averageBalance;
        }
    }

    private static Transactions getFirstBalance(List<Transactions> transactionsList) {


        LocalDateTime yesterdayMidNight = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();

        List<Transactions> oldTransactions = new ArrayList<>(transactionsList.stream().filter(transaction -> (
                yesterdayMidNight.isAfter(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) ||
                        yesterdayMidNight.isEqual(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
        ).toList());

        if (oldTransactions.isEmpty()) {
            return null;
        } else {
            oldTransactions.sort(Comparator.comparing(Transactions::getUpdated_at));
            return oldTransactions.get(oldTransactions.size() - 1);
        }
    }
}
