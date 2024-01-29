package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.constants.ProductType;
import et.kacha.interestcalculating.entity.Transactions;

import java.util.*;

public class BalanceUtility {
    public static int calculateMinimumBalance(List<Transactions> transactionsList,
                                              ProductType pt,
                                              Calendar currentDate,
                                              Calendar firstDayOfMonth,
                                              Calendar lastDayOfMonth) {


        Transactions firstTransaction = getFirstBalance(transactionsList, firstDayOfMonth);
        if (Objects.isNull(firstTransaction)) {
            return 0;
        } else {
            int minimiumBalance = 0;
            minimiumBalance = firstTransaction.getBalance();
            List<Transactions> newTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                    firstDayOfMonth.before(transaction.getUpdated_at_time())).toList());
            for (Transactions transaction : newTransactions) {
                if (minimiumBalance < transaction.getBalance()) {
                    minimiumBalance = transaction.getBalance();
                }
            }
            return minimiumBalance;
        }


    }

    private static Transactions getFirstBalance(List<Transactions> transactionsList,
                                                Calendar firstDayOfMonth) {
        List<Transactions> oldTransactions = new ArrayList<>(transactionsList.stream().filter(transaction ->
                !firstDayOfMonth.before(transaction.getUpdated_at_time())).toList());
        if (oldTransactions.isEmpty()) {
            return null;
        } else {
            oldTransactions.sort(Comparator.comparing(Transactions::getUpdated_at_time));
            return oldTransactions.get(oldTransactions.size() - 1);
        }
    }
}
