package et.kacha.interestcalculating.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import et.kacha.interestcalculating.constants.*;
import et.kacha.interestcalculating.dto.*;
import et.kacha.interestcalculating.dto.callback.CallBackResponse;
import et.kacha.interestcalculating.entity.*;
import et.kacha.interestcalculating.repository.*;
import et.kacha.interestcalculating.service.ManualWithdrawalService;
import et.kacha.interestcalculating.service.ProcessCallBackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("interest/")
@Slf4j
public class MainController {

    private final ObjectMapper objectMapper;

    private final ProductsRepository productsRepository;

    private final SubscriptionsRepository subscriptionsRepository;

    private final CustomersRepository customersRepository;

    private final TransactionsRepository transactionsRepository;

    private final ChargesRepository chargesRepository;

    private final ChargeFeesRepository chargeFeesRepository;

    private final ManualWithdrawalService manualWithdrawalService;

    private final ProcessCallBackService processCallBackService;

    @PostMapping("/process")
    MainInterestRequest processInterestManual(@RequestBody MainRequest interestBody) {

        try {
            log.info("On demand Interest calculation request | {}", new ObjectMapper().writeValueAsString(interestBody));

            InterestRequest interestRequest = objectMapper.convertValue(interestBody.getPayload(), InterestRequest.class);

            MainInterestRequest mainResponse = manualWithdrawalService.calculateUnpaidInterest(interestRequest, interestBody,true);

            log.info("On demand Interest calculation response | {}", new ObjectMapper().writeValueAsString(mainResponse));

            return mainResponse;

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id("")
                    .callbackUrl("")
                    .payload(Arrays.asList(InterestBody.builder()
                            .subscriptionId("")
                            .interestAmount(0)
                            .taxAmount(0)
                            .chargeAmount(0)
                            .txnRef(null)
                            .phone(null)
                            .build()))
                    .build();
        }
    }
    @PostMapping("/validate")
    MainInterestRequest validateInterestManual(@RequestBody MainRequest interestBody) {

        try {
            log.info("On demand Interest validation request | {}", new ObjectMapper().writeValueAsString(interestBody));

            InterestRequest interestRequest = objectMapper.convertValue(interestBody.getPayload(), InterestRequest.class);

            MainInterestRequest mainResponse = manualWithdrawalService.calculateUnpaidInterest(interestRequest, interestBody,false);

            log.info("On demand Interest validation response | {}", new ObjectMapper().writeValueAsString(mainResponse));

            return mainResponse;

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return MainInterestRequest.builder()
                    .commandId("PayInterest")
                    .fi_id("")
                    .callbackUrl("")
                    .payload(Arrays.asList(InterestBody.builder()
                            .subscriptionId("")
                            .interestAmount(0)
                            .taxAmount(0)
                            .chargeAmount(0)
                            .txnRef(null)
                            .phone(null)
                            .build()))
                    .build();
        }
    }

    @PostMapping("/callback")
    MainResponse processCallBack(@RequestBody CallBackResponse callBackBody) {

        try {
            log.info("Call back returned | {}", new ObjectMapper().writeValueAsString(callBackBody));

            MainResponse mainResponse = processCallBackService.processInterestCallBack(callBackBody);

            log.info("Call back response | {}", new ObjectMapper().writeValueAsString(mainResponse));

            return mainResponse;

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();

        }
    }

    @PostMapping("/generate")
    MainResponse generateInterestData(@RequestBody MainRequest interestBody) {

        try {
            log.info("On demand Interest calculation request | {}", new ObjectMapper().writeValueAsString(interestBody));

            InterestRequest interestRequest = objectMapper.convertValue(interestBody.getPayload(), InterestRequest.class);

            InterestBody rsp = InterestBody.builder()
                    .phone(interestRequest.getMsisdn())
                    .interestAmount(0)
                    .build();

          /*  Products product = productsRepository.save(Products.builder()
                    .product_name("Personal loan")
                    .product_description("Product description")
                    .decimal_places(2)
                    .min_deposit_amt(2000)
                    .max_deposit_amt(2000000)
                    .interest_rate(7f)
                    .term_duration(3)
                    .interest_comp_type(InterestCompType.MONTHLY)
                    .interest_posting_period(3)
                    .interest_calculated_using(InterestCalculatedUsing.MIN_BALANCE)
                    .min_opening_balance(2000)
                    .max_saving_limit(2000000)
                    .min_deposit_term(3)
                    .max_deposit_term(6)
                    .is_tax_available(true)
                    .tax_fee_type(ChargeRate.PERCENTAGE)
                    .tax_fee_amount(3)
                    .min_interest_bearing_amt(2000)
                    .product_type(ProductType.REGULAR)
                    .interest_type(InterestType.IB)
                    .state(ProductState.ACTIVE)
                    .days_for_inactivity(365)
                    .days_for_dormancy(365)
                    .is_tax_available(true)
                    .tax_fee_type(ChargeRate.PERCENTAGE)
                    .tax_fee_amount(3)
                    .financial_institution_id(1)
                    .added_by(1)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .build());*/

            Products product = productsRepository.save(Products.builder()
                    .product_name("Timed deposit")
                    .product_description("Product description")
                    .decimal_places(2)
                    .min_deposit_amt(2000f)
                    .max_deposit_amt(2000000f)
                    .interest_rate(7f)
                    .term_duration(3)
                    .interest_comp_type(InterestCompType.DAILY)
                    .interest_posting_period(3)
                    .interest_calculated_using(InterestCalculatedUsing.MIN_BALANCE)
                    .min_opening_balance(2000f)
                    .max_saving_limit(2000000f)
                    .min_deposit_term(3)
                    .max_deposit_term(6)
                    .is_tax_available(true)
                    .tax_fee_type(ChargeRate.PERCENTAGE)
                    .tax_fee_amount(3f)
                    .min_interest_bearing_amt(2000f)
                    .product_type(ProductType.TIME)
                    .interest_type(InterestType.IB)
                    .state(ProductState.ACTIVE)
                    .days_for_inactivity(365)
                    .days_for_dormancy(365)
                    .is_tax_available(true)
                    .tax_fee_type(ChargeRate.PERCENTAGE)
                    .tax_fee_amount(3f)
                    .financial_institution_id(1)
                    .added_by(1)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .build());
            Charge charge = chargesRepository.save(Charge.builder()
                    .charge_name("Charge name")
                    .charge_for(ChargeFor.TRB)
                    .charge_type(ChargeType.Charge)
                    .charge_calculation_type(ChargeRate.PERCENTAGE)
                    .charge_payment_mode(ChargePaymentMode.ONETIME)
                    .approved_by(1)
                    .added_by(1)
                    .products(product)
                    .status(ChargeState.ACTIVE)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .build());

            ChargeFees chargeFees = chargeFeesRepository.save(ChargeFees.builder()
                    .charge_amount(5f)
                    .charge(charge)
                    .approved_by(1)
                    .added_by(1)
                    .status(ChargeState.ACTIVE)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .build());
            Subscriptions subscriptions = subscriptionsRepository.save(Subscriptions.builder()
                    .product(product)
                    .balance(3000.0f)
                    .customer(customersRepository.findById(1).get())
                    .phone("0917498840")
                    .status(SubscriptionStatus.ACTIVE)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .build());
            Transactions transactions = transactionsRepository.save(Transactions.builder()
                    .amount(2000f)
                    .balance(1000f)
                    .bat(56565f)
                    .bbt(5765f)
                    .txn_ref("GFJHGESSGFGTY")
                    .txn_id("GHGRCGHLKUSBYGYT")
                    .transaction_type(TransactionType.DEPOSIT)
                    .subscriptions(subscriptions)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .status(TransactionStatus.SUCCESS)
                    .build());
           /* transactions = transactionsRepository.save(Transactions.builder()
                    .amount(2000)
                    .balance(5000)
                    .bat(56565)
                    .bbt(5765)
                    .txn_ref("GHGREGDSTECSBH")
                    .txn_id("GHGRGEDSEGCSBH")
                    .transaction_type(TransactionType.DEPOSIT)
                    .subscriptions(subscriptions)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .status(TransactionStatus.SUCCESS)
                    .build());
            transactions = transactionsRepository.save(Transactions.builder()
                    .amount(2000)
                    .balance(7000)
                    .bat(56565)
                    .bbt(5765)
                    .txn_ref("GHGRCSGFJMJBH")
                    .txn_id("WGTJUUJUEW")
                    .transaction_type(TransactionType.DEPOSIT)
                    .subscriptions(subscriptions)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .status(TransactionStatus.SUCCESS)
                    .build());
            transactions = transactionsRepository.save(Transactions.builder()
                    .amount(2000)
                    .balance(9000)
                    .bat(56565)
                    .bbt(5765)
                    .txn_ref("GHGRTRNHNHCSBH")
                    .txn_id("GHGRTRJJCSBH")
                    .transaction_type(TransactionType.DEPOSIT)
                    .subscriptions(subscriptions)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .status(TransactionStatus.SUCCESS)
                    .build());
            transactions = transactionsRepository.save(Transactions.builder()
                    .amount(2000)
                    .balance(11000)
                    .bat(56565)
                    .bbt(5765)
                    .txn_ref("GHGRTRWWWCSBH")
                    .txn_id("GHGWTRWWRCSBH")
                    .transaction_type(TransactionType.DEPOSIT)
                    .subscriptions(subscriptions)
                    .created_at(new Date())
                    .updated_at(new Date())
                    .status(TransactionStatus.SUCCESS)
                    .build());*/
            log.info(new ObjectMapper().writeValueAsString(interestRequest));

            return MainResponse.builder()
                    .id(interestBody.getId())
                    .responseDesc("Successfully processed!")
                    .responseCode("0")
                    .payload(rsp)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
