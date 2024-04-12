package et.kacha.interestcalculating.scheduled_tasks;

import et.kacha.interestcalculating.constants.USSDStatus;
import et.kacha.interestcalculating.entity.CashoutPendingTransaction;
import et.kacha.interestcalculating.repository.CashoutPendingTransactionRepo;
import et.kacha.interestcalculating.util.SendInterestPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class USSDStatusScheduler {

    private final CashoutPendingTransactionRepo cashoutPendingTransactionRepo;

    private final SendInterestPaymentUtil sendInterestPaymentUtil;

    @Scheduled(cron = "0 */20 * * * *")
    public void scheduledUSSDSearch() {

        List<CashoutPendingTransaction> cashoutPendingTransactions = cashoutPendingTransactionRepo.findByStatus(USSDStatus.PENDING);

        ArrayList<Integer> ids = new ArrayList<>();

        for (CashoutPendingTransaction cahoutTRX : cashoutPendingTransactions) {
            ids.add(cahoutTRX.getId());
        }

        if (!ids.isEmpty()) {

            log.info("Scheduled ussd service started.");

            sendInterestPaymentUtil.sendUSSDRequest(ids);

            log.info("Scheduled ussd service ended.");
        }
    }

}
