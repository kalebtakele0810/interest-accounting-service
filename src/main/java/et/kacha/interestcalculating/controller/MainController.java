package et.kacha.interestcalculating.controller;


import et.kacha.interestcalculating.dto.ApiResponse;
import et.kacha.interestcalculating.dto.WebHookRequest;
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

    @PostMapping("/process")
    ApiResponse processInterest(@RequestBody WebHookRequest notifyRequest) {

        log.info("Interest calculation {} | for customer {}",
                notifyRequest.getId(), notifyRequest.getAccount_name());

        return ApiResponse.builder()
                .id(notifyRequest.getId())
                .responseDesc("Successfully processed!")
                .responseCode("0")
                .build();

    }


}
