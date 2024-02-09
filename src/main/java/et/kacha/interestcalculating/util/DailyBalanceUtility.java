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

            LocalDateTime todayMidNight = LocalDateTime.now().toLocalDate().atStartOfDay();

            List<Transactions> newTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                    todayMidNight.isBefore(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())).toList());

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

            LocalDateTime todayMidNight = LocalDateTime.now().toLocalDate().atStartOfDay();

            List<Transactions> newTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                    todayMidNight.isBefore(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())).toList());

            int index = 1;
            for (Transactions transaction : newTransactions) {
                averageBalance = (averageBalance + transaction.getBalance()) / ++index;
            }
            return averageBalance;
        }
    }

    private static Transactions getFirstBalance(List<Transactions> transactionsList) {

        LocalDateTime todayMidNight = LocalDateTime.now().toLocalDate().atStartOfDay();

        List<Transactions> oldTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                todayMidNight.isAfter(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) ||
                        todayMidNight.isEqual(transaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())).toList());

        if (oldTransactions.isEmpty()) {
            return null;
        } else {
            oldTransactions.sort(Comparator.comparing(Transactions::getUpdated_at));
            return oldTransactions.get(oldTransactions.size() - 1);
        }
    }
}
