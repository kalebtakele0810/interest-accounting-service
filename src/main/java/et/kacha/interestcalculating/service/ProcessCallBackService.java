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

        for (Payload payload : callBackBody.getPayload()) {
            List<InterestHistory> unProcessedInterestPayments =
                    interestHistoryRepository.findByRequestRefIdAndStatus(payload.getRequestRefId(), InterestPaymentState.WAITING);
            for (InterestHistory interestHistory : unProcessedInterestPayments) {
                try {
                    List<InterestFeeHistory> interestFees = interestFeeHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

                    List<TaxHistory> interestTaxes = interestTaxHistoryRepository.findByInterestHistoryIdAndStatus(interestHistory.getId(), InterestPaymentState.SAVED);

                    interestHistory.setStatus(payload.getResponseCode() == 201 ? InterestPaymentState.PAID : InterestPaymentState.SAVED);
                    interestHistoryRepository.save(interestHistory);

                    for (InterestFeeHistory fee : interestFees) {
                        fee.setStatus(payload.getResponseCode() == 201 ? InterestPaymentState.PAID : InterestPaymentState.SAVED);
                        interestFeeHistoryRepository.save(fee);
                    }

                    for (TaxHistory tax : interestTaxes) {
                        tax.setStatus(payload.getResponseCode() == 201 ? InterestPaymentState.PAID : InterestPaymentState.SAVED);
                        interestTaxHistoryRepository.save(tax);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
