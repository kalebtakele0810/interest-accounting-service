package et.kacha.interestcalculating.scheduled_tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.dto.*;
import et.kacha.interestcalculating.entity.InterestFeeHistory;
import et.kacha.interestcalculating.entity.InterestHistory;
import et.kacha.interestcalculating.entity.TaxHistory;
import et.kacha.interestcalculating.repository.InterestFeeHistoryRepository;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import et.kacha.interestcalculating.repository.InterestTaxHistoryRepository;
import et.kacha.interestcalculating.util.SendInterestPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessPaymentsScheduler {

    private final InterestHistoryRepository interestHistoryRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    private final SendInterestPaymentUtil sendInterestPaymentUtil;

    @Scheduled(cron = "0 0 2 * * *", zone = "GMT+3")
    public void searchScheduledInterestPayments() {

        log.info("Scheduled interest payment started.");

        List<InterestHistory> unProcessedInterestPayments = interestHistoryRepository.findByStatus(InterestPaymentState.SAVED);
        for (InterestHistory interestHistory : unProcessedInterestPayments) {
            try {
                List<InterestFeeHistory> interestFees = interestFeeHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

                List<TaxHistory> interestTaxes = interestTaxHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

                double taxAmount = 0, chargeAmount = 0;

                for (InterestFeeHistory fee : interestFees) {
                    chargeAmount += fee.getAmount();
                }

                for (TaxHistory tax : interestTaxes) {
                    taxAmount += tax.getAmount();
                }

                MainRequest mainRequest = MainRequest.builder()
                        .id(String.valueOf(UUID.randomUUID()))
                        .commandId("PayInterest")
                        .payload(InterestBody.builder()
                                .subscriptionId(String.valueOf(interestHistory.getSubscriptions().getId()))
                                .interestAmount(interestHistory.getAmount())
                                .taxAmount(taxAmount)
                                .chargeAmount(chargeAmount)
                                .fiId(String.valueOf(interestHistory.getSubscriptions().getProduct().getFinancial_institution_id()))
                                .txnRef(String.valueOf(UUID.randomUUID()))
                                .phone(String.valueOf(interestHistory.getSubscriptions().getId()))
                                .build())
                        .build();
                log.info("Sending interest payment | interest history Id " + interestHistory.getId() + " | amount "
                        + interestHistory.getAmount() + " | " + new ObjectMapper().writeValueAsString(mainRequest));

                String mainResponse = sendInterestPaymentUtil.sendPaymentRequest(mainRequest);

                log.info("Response of interest payment | interest history Id " + interestHistory.getId() + " | response " + interestHistory.getAmount()
                        + " | " + mainResponse);

                if (Objects.nonNull(mainResponse)) {

                    interestHistory.setStatus(InterestPaymentState.PAID);
                    interestHistoryRepository.save(interestHistory);

                    for (InterestFeeHistory fee : interestFees) {
                        fee.setStatus(InterestPaymentState.PAID);
                        interestFeeHistoryRepository.save(fee);
                    }

                    for (TaxHistory tax : interestTaxes) {
                        tax.setStatus(InterestPaymentState.PAID);
                        interestTaxHistoryRepository.save(tax);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Scheduled interest payment ended.");
    }
    /*

    @Scheduled(cron = "0 30 12 * * *", zone = "GMT+3")
    public void searchScheduledTAXPayments() {

        log.info("Scheduled tax payment started.");

        List<InterestHistory> unProcessedInterestPayments = interestHistoryRepository.findByStatus(InterestPaymentState.SAVED);
        for (InterestHistory interestHistory : unProcessedInterestPayments) {

            //Call payment API

            boolean isPaymentSuccessful = true;
            if (isPaymentSuccessful) {
                interestHistory.setStatus(InterestPaymentState.PAID);
                interestHistoryRepository.save(interestHistory);
            }
        }

        log.info("Scheduled tax payment ended.");
    }

    @Scheduled(cron = "0 30 13 * * *", zone = "GMT+3")
    public void searchScheduledChargePayments() {

        log.info("Scheduled charge payment started.");

        List<InterestHistory> unProcessedInterestPayments = interestHistoryRepository.findByStatus(InterestPaymentState.SAVED);
        for (InterestHistory interestHistory : unProcessedInterestPayments) {

            //Call payment API

            boolean isPaymentSuccessful = true;
            if (isPaymentSuccessful) {
                interestHistory.setStatus(InterestPaymentState.PAID);
                interestHistoryRepository.save(interestHistory);
            }
        }

        log.info("Scheduled charge payment ended.");
    }*/
}
