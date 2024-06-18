package et.kacha.interestcalculating.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.kacha.interestcalculating.dto.MainRequest;
import et.kacha.interestcalculating.dto.MainResponse;
import et.kacha.interestcalculating.dto.whitelist.WhitelistRequest;
import et.kacha.interestcalculating.service.WhitelistService;
import et.kacha.interestcalculating.util.SendInterestPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("whitelist")
@Slf4j
public class WhitelistController {

    private final ObjectMapper objectMapper;

    private final WhitelistService whitelistService;

    private final SendInterestPaymentUtil sendInterestPaymentUtil;

    @PostMapping("/bulk-upload")
    MainResponse processInterestManual(@RequestParam("file") MultipartFile file,@RequestParam("product_id") String productId, @RequestHeader("Authorization") String authorization) {

        try {
            log.info("Bulk-upload request | {}", file.getName());

            String token = authorization.split(" ")[1];

            MainResponse rsp = whitelistService.processBulkUpload(file, productId, token);

            log.info("Bulk-upload response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/save")
    MainResponse addWhitelist(@RequestBody MainRequest interestBody, @RequestHeader("Authorization") String authorization) {
        //
        try {
            log.info("Saving whitelist request token " + authorization.split(" ")[1]);

            String token = authorization.split(" ")[1];

            log.info("Save whitelist request | {}", new ObjectMapper().writeValueAsString(interestBody));

            WhitelistRequest whitelistRequest = objectMapper.convertValue(interestBody.getPayload(), WhitelistRequest.class);

            MainResponse rsp = whitelistService.saveWhiteList(whitelistRequest, token);

            log.info("Save whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {
            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }
    }

    @PostMapping("/approve")
    MainResponse approveWhiteList(@RequestBody MainRequest interestBody, @RequestHeader("Authorization") String authorization) {
        try {
            log.info("Approve whitelist request | {}", new ObjectMapper().writeValueAsString(interestBody));

            String token = authorization.split(" ")[1];

            WhitelistRequest whitelistRequest = objectMapper.convertValue(interestBody.getPayload(), WhitelistRequest.class);

            MainResponse rsp = whitelistService.approveWhiteList(whitelistRequest, token);

            log.info("Approve whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {
            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }
    }

    @PostMapping("/decline")
    MainResponse declineWhiteList(@RequestBody MainRequest interestBody, @RequestHeader("Authorization") String authorization) {
        try {

            log.info("Decline whitelist request | {}", new ObjectMapper().writeValueAsString(interestBody));

            String token = authorization.split(" ")[1];

            WhitelistRequest whitelistRequest = objectMapper.convertValue(interestBody.getPayload(), WhitelistRequest.class);

            MainResponse rsp = whitelistService.declineWhiteList(whitelistRequest, token);

            log.info("Decline whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {
            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }
    }

    @PostMapping("/delete")
    MainResponse deleteWhiteList(@RequestBody MainRequest interestBody, @RequestHeader("Authorization") String authorization) {
        try {

            log.info("Delete whitelist request | {}", new ObjectMapper().writeValueAsString(interestBody));

            String token = authorization.split(" ")[1];

            WhitelistRequest whitelistRequest = objectMapper.convertValue(interestBody.getPayload(), WhitelistRequest.class);

            MainResponse rsp = whitelistService.deleteWhiteList(whitelistRequest, token);

            log.info("Delete whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {

            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();

        }
    }

    @GetMapping("/{id}")
    MainResponse getSingleWhiteList(@PathVariable("id") Integer whiteListId, @RequestHeader("Authorization") String authorization) {
        try {

            String token = authorization.split(" ")[1];

            log.info("Fetch whitelist request | {}", whiteListId);

            MainResponse rsp = whitelistService.getSingleWhiteList(whiteListId,token);

            log.info("Fetch whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {

            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();

        }
    }

    @GetMapping("")
    MainResponse getWhiteList(@RequestHeader("Authorization") String authorization) {
        try {
            log.info("Fetch FI whitelist request");

            String token = authorization.split(" ")[1];

            MainResponse rsp = whitelistService.getFIWhiteList(token);

            log.info("Fetch FI whitelist response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {

            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();

        }
    }


    @GetMapping("/check")
    MainResponse getSingleWhiteListByPhone(@RequestParam String phone, @RequestParam Integer productId) {
        try {


            log.info("Fetch whitelist request by phone | {}", phone);

            MainResponse rsp = whitelistService.getSingleWhiteListByPhone(phone, productId);

            log.info("Fetch whitelist by phone response | {}", new ObjectMapper().writeValueAsString(rsp));

            return rsp;

        } catch (Exception e) {

            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();

        }
    }
}
