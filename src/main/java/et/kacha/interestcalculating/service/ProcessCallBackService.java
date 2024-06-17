package et.kacha.interestcalculating.service;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.dto.MainResponse;
import et.kacha.interestcalculating.dto.callback.CallBackResponse;
import et.kacha.interestcalculating.dto.callback.Payload;
import et.kacha.interestcalculating.entity.InterestFeeHistory;
import et.kacha.interestcalculating.entity.InterestHistory;
import et.kacha.interestcalculating.entity.TaxHistory;
import et.kacha.interestcalculating.repository.InterestFeeHistoryRepository;
import et.kacha.interestcalculating.repository.InterestHistoryRepository;
import et.kacha.interestcalculating.repository.InterestTaxHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessCallBackService {

    private final InterestHistoryRepository interestHistoryRepository;

    private final InterestFeeHistoryRepository interestFeeHistoryRepository;

    private final InterestTaxHistoryRepository interestTaxHistoryRepository;

    public MainResponse processInterestCallBack(CallBackResponse callBackBody) {

//        for (Payload payload : callBackBody.getPayload()) {
        List<InterestHistory> unProcessedInterestPayments =
                interestHistoryRepository.findByRequestRefIdAndStatus(callBackBody.getTrace_number(), InterestPaymentState.WAITING);
        for (InterestHistory interestHistory : unProcessedInterestPayments) {
            try {
                List<InterestFeeHistory> interestFees = interestFeeHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.WAITING);

                List<TaxHistory> interestTaxes = interestTaxHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.WAITING);

                interestHistory.setStatus(callBackBody.getStatus().equalsIgnoreCase("PROCESSED") ? InterestPaymentState.PAID : InterestPaymentState.WAITING);
                interestHistoryRepository.save(interestHistory);

                for (InterestFeeHistory fee : interestFees) {
                    fee.setStatus(callBackBody.getStatus().equalsIgnoreCase("PROCESSED") ? InterestPaymentState.PAID : InterestPaymentState.WAITING);
                    interestFeeHistoryRepository.save(fee);
                }

                for (TaxHistory tax : interestTaxes) {
                    tax.setStatus(callBackBody.getStatus().equalsIgnoreCase("PROCESSED") ? InterestPaymentState.PAID : InterestPaymentState.WAITING);
                    interestTaxHistoryRepository.save(tax);
                }

            } catch (Exception e) {
                e.printStackTrace();
//                }
            }
        }

        return MainResponse.builder()
                .responseDesc("SUCCESS")
                .responseCode("0")
                .build();
    }
}
