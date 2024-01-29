package et.kacha.interestcalculating.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.kacha.interestcalculating.dto.InterestRequest;
import et.kacha.interestcalculating.dto.InterestResponse;
import et.kacha.interestcalculating.dto.MainResponse;
import et.kacha.interestcalculating.dto.MainRequest;
import et.kacha.interestcalculating.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("interest/")
@Slf4j
public class MainController {

    private final ObjectMapper objectMapper;
    @PostMapping("/process")
    MainResponse processInterest(@RequestBody MainRequest intrestBody) {

        try {
            log.info("Interest calculation {} | for customer {}",
                    new ObjectMapper().writeValueAsString(intrestBody));


            InterestRequest interestRequest = objectMapper.convertValue(intrestBody.getPayload(), InterestRequest.class);

            InterestResponse rsp = InterestResponse.builder()
                    .msisdn(interestRequest.getMsisdn())
                    .interestAmount(0)
                    .build();

            log.info(             new ObjectMapper().writeValueAsString(interestRequest));

            return MainResponse.builder()
                    .id(intrestBody.getId())
                    .responseDesc("Successfully processed!")
                    .responseCode("0")
                    .payload(rsp)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
