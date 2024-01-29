package et.kacha.interestcalculating.util;

import et.kacha.interestcalculating.constants.ProductType;
import et.kacha.interestcalculating.entity.Transactions;

import java.util.List;
import java.util.Optional;

public class InterestUtility {
   /* public static int calculateInterest(List<Transactions> transactionsList, ProductType pt){

        int minimiumBalance = 0;

        Optional<Transactions> firstTransaction = transactionsList.stream().findFirst();

        if (firstTransaction.isPresent()) {
            minimiumBalance = firstTransaction.get().getBalance();
            for (Transactions transaction : transactionsList) {
                minimiumBalance = transaction.getBalance();
            }
        }

        return minimiumBalance;
    }*/
}
