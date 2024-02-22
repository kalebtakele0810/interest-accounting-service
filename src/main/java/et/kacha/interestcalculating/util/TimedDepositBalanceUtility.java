package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Transactions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class TimedDepositBalanceUtility {
    public static float calculateMinimumBalance(List<Transactions> transactionsList, Products product) {

        Transactions firstTransaction = getFirstBalance(transactionsList);
        if (Objects.isNull(firstTransaction)) {
            return 0;
        } else {
            float minimiumBalance = 0;
            LocalDate transactionDate = firstTransaction.getUpdated_at().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
            LocalDate dueDate = transactionDate.plusMonths(product.getTerm_duration());
            if (dueDate.isEqual(LocalDate.now())) {
                minimiumBalance = firstTransaction.getBalance();
            }

            ///if any withdraws occur set the minimum balance to zero
            for (Transactions transaction : transactionsList) {
                if (minimiumBalance > transaction.getBalance()) {
                    minimiumBalance = 0;
                }
            }
            return minimiumBalance;

        }
    }

    private static Transactions getFirstBalance(List<Transactions> transactionsList) {

        if (transactionsList.isEmpty()) {
            return null;
        } else {
            Collections.sort(transactionsList, Comparator.comparing(Transactions::getUpdated_at));
            return transactionsList.get(0);
        }
    }
}
