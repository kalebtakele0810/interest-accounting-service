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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessPaymentsScheduler {

    private final InterestHistoryRepository interestHistoryRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    private final SendInterestPaymentUtil sendInterestPaymentUtil;

    @Value("${interest.callback.url}")
    private String interestCallbackUrl;

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
                String requestRefId = String.valueOf(UUID.randomUUID());

                MainInterestRequest mainRequest = MainInterestRequest.builder()
//                        .id(String.valueOf(UUID.randomUUID()))
                        .commandId("PayInterest")
                        .fi_id(String.valueOf(interestHistory.getSubscriptions().getProduct().getFinancial_institution_id()))
                        .callbackUrl(interestCallbackUrl)
                        .payload(new ArrayList<InterestBody>(Arrays.asList(InterestBody.builder()
                                .subscriptionId(String.valueOf(interestHistory.getSubscriptions().getId()))
                                .interestAmount(interestHistory.getInterest_after_deduction())
                                .taxAmount(taxAmount)
                                .chargeAmount(chargeAmount)
//                                .fiId(String.valueOf(interestHistory.getSubscriptions().getProduct().getFinancial_institution_id()))
                                .txnRef(requestRefId)
                                .phone(String.valueOf(interestHistory.getSubscriptions().getPhone()))
                                .build())))
                        .build();
                log.info("Sending interest payment | interest history Id " + interestHistory.getId() + " | amount "
                        + interestHistory.getInterest_after_deduction() + " | " + new ObjectMapper().writeValueAsString(mainRequest));

                String mainResponse = sendInterestPaymentUtil.sendPaymentRequest(mainRequest);

                log.info("Response of interest payment | interest history Id " + interestHistory.getId() + " | response " + interestHistory.getInterest_after_deduction()
                        + " | " + mainResponse);

                if (Objects.nonNull(mainResponse)) {

//                    interestHistory.setStatus(InterestPaymentState.WAITING);
                    interestHistory.setStatus(InterestPaymentState.PAID);
                    interestHistory.setRequestRefId(requestRefId);
                    interestHistoryRepository.save(interestHistory);

                    for (InterestFeeHistory fee : interestFees) {
//                        fee.setStatus(InterestPaymentState.WAITING);
                        fee.setStatus(InterestPaymentState.PAID);
                        interestFeeHistoryRepository.save(fee);
                    }

                    for (TaxHistory tax : interestTaxes) {
//                        tax.setStatus(InterestPaymentState.WAITING);
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
}
