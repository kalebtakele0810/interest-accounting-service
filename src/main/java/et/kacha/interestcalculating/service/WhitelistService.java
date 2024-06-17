package et.kacha.interestcalculating.service;

import et.kacha.interestcalculating.constants.ChargeState;
import et.kacha.interestcalculating.dto.MainResponse;
import et.kacha.interestcalculating.dto.whitelist.WhitelistRequest;
import et.kacha.interestcalculating.dto.whitelist.login.MainLoginResponse;
import et.kacha.interestcalculating.entity.Products;
import et.kacha.interestcalculating.entity.Whitelist;
import et.kacha.interestcalculating.repository.ProductsRepository;
import et.kacha.interestcalculating.repository.WhitelistRepository;
import et.kacha.interestcalculating.util.SendInterestPaymentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class WhitelistService {

    private final String COMMA_DELIMITER = ",";

    private final ProductsRepository productsRepository;

    private final WhitelistRepository whitelistRepository;

    private final SendInterestPaymentUtil sendInterestPaymentUtil;


    public MainResponse processBulkUpload(MultipartFile multipartFile, String productId, String token) throws IOException {
        MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
        if (Objects.isNull(mainLoginResponse)
                || Objects.isNull(mainLoginResponse.getData())
                || Objects.isNull(mainLoginResponse.getData().getRole())
                || Objects.isNull(mainLoginResponse.getData().getFi_id())
                || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
            return MainResponse.builder()
                    .responseDesc("Unauthorized.")
                    .responseCode("1")
                    .build();
        }
        if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("MAKER")) {
            return MainResponse.builder()
                    .responseDesc("Unauthorized.")
                    .responseCode("1")
                    .build();
        }
        List<List<String>> records = new ArrayList<>();
        int index = 1;

        InputStream initialStream = multipartFile.getInputStream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        Optional<Products> productById = null;
        File targetFile = new File(String.valueOf(UUID.randomUUID()) + ".tmp");
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(buffer);
        }
        try (Scanner scanner = new Scanner(targetFile)) {
            while (scanner.hasNextLine()) {

                List<String> recordFromLine = getRecordFromLine(scanner.nextLine());

                if (recordFromLine.size() == 2) {

                    if (index > 1 && !isValidPhoneNumber(recordFromLine.get(0))) {

                        return MainResponse.builder()
                                .responseDesc("Invalid phone number format at line " + index)
                                .responseCode("1")
                                .build();
                    }

                    if (index > 1) {
                        String productID = recordFromLine.get(1).trim();
                        try {
                            productById = productsRepository.findById(Integer.parseInt(productID));
                        } catch (NumberFormatException e) {
                            return MainResponse.builder()
                                    .responseDesc("Parsing exception at line " + index)
                                    .responseCode("1")
                                    .build();
                        }

                        if (productById.isEmpty()) {
                            return MainResponse.builder()
                                    .responseDesc("Product Id can't be found at line " + index)
                                    .responseCode("1")
                                    .build();
                        }
                        Whitelist byPhoneAndProductsId = whitelistRepository.findByPhoneAndProducts_Id(recordFromLine.get(0), Integer.parseInt(productID));
                        if (!Objects.isNull(byPhoneAndProductsId)) {
                            return MainResponse.builder()
                                    .responseDesc("Customer at line " + index + " already whitelisted.")
                                    .responseCode("1")
                                    .build();
                        }
                        records.add(recordFromLine);
                    }

                    index++;

                } else {
                    return MainResponse.builder()
                            .responseDesc("Parsing exception at line " + index)
                            .responseCode("1")
                            .build();
                }
            }
            for (List<String> recordFromLine : records) {
                String phoneNumber = recordFromLine.get(0).trim();
                String productID = recordFromLine.get(1).trim();
                whitelistRepository.save(Whitelist.builder()
                        .phone(phoneNumber)
                        .status(ChargeState.PENDING)
                        .added_by(mainLoginResponse.getData().getUser_id())
                        .fi_id(mainLoginResponse.getData().getFi_id())
                        .products(productById.get())
                        .build());
                log.info("Processing record from phone number " + phoneNumber + " and product id " + productID);
                index++;

            }

            targetFile.delete();
            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .build();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return MainResponse.builder()
                    .responseDesc("Parsing exception at line " + index)
                    .responseCode("1")
                    .build();
        }
    }


    public MainResponse saveWhiteList(WhitelistRequest whitelistRequest, String token) {
        try {
            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("MAKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            Optional<Products> productById = productsRepository.findById(whitelistRequest.getProductId());


            if (productById.isEmpty()) {
                return MainResponse.builder()
                        .responseDesc("Product Id can't be found")
                        .responseCode("1")
                        .build();
            }
            Whitelist byPhoneAndProductsId = whitelistRepository.findByPhoneAndProducts_Id(whitelistRequest.getMsisdn(), whitelistRequest.getProductId());

            if (!Objects.isNull(byPhoneAndProductsId)) {
                return MainResponse.builder()
                        .responseDesc("Customer is already whitelisted.")
                        .responseCode("1")
                        .build();
            }

            whitelistRepository.save(Whitelist.builder()
                    .phone(whitelistRequest.getMsisdn())
                    .status(ChargeState.PENDING)
                    .added_by(mainLoginResponse.getData().getUser_id())
                    .fi_id(mainLoginResponse.getData().getFi_id())
                    .products(productById.get())
                    .build());


            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .build();
        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }


    public MainResponse approveWhiteList(WhitelistRequest whitelistRequest, String token) {
        try {
            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            Optional<Whitelist> whitelistById = whitelistRepository.findById(whitelistRequest.getWhitelistId());

            if (whitelistById.isEmpty()) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }
            Whitelist whitelist = whitelistById.get();

            if (whitelist.getStatus() == ChargeState.ACTIVE) {
                return MainResponse.builder()
                        .responseDesc("Whitelist already approved.")
                        .responseCode("1")
                        .build();
            }

            whitelist.setApproved_by(mainLoginResponse.getData().getUser_id());
            whitelist.setStatus(ChargeState.ACTIVE);

            whitelistRepository.save(whitelist);


            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .build();
        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }


    public MainResponse declineWhiteList(WhitelistRequest whitelistRequest, String token) {
        try {

            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }

            Optional<Whitelist> whitelistById = whitelistRepository.findById(whitelistRequest.getWhitelistId());

            if (whitelistById.isEmpty()) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }
            Whitelist whitelist = whitelistById.get();

            if (whitelist.getStatus() == ChargeState.DECLINED) {
                return MainResponse.builder()
                        .responseDesc("Whitelist already declined.")
                        .responseCode("1")
                        .build();
            }

            whitelist.setApproved_by(mainLoginResponse.getData().getUser_id());
            whitelist.setStatus(ChargeState.DECLINED);

            whitelistRepository.save(whitelist);


            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .build();
        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }

    public MainResponse deleteWhiteList(WhitelistRequest whitelistRequest, String token) {
        try {

            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            /*if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }*/

            Optional<Whitelist> whitelistById = whitelistRepository.findById(whitelistRequest.getWhitelistId());

            if (whitelistById.isEmpty()) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }

            Whitelist whitelist = whitelistById.get();

            whitelistRepository.delete(whitelist);

            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .build();
        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }

    public MainResponse getSingleWhiteList(Integer whitelistId, String token) {
        try {
            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
           /* if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }*/
            Optional<Whitelist> whitelistById = whitelistRepository.findById(whitelistId);

            if (whitelistById.isEmpty()) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }

            Whitelist whitelist = whitelistById.get();

            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .payload(whitelist)
                    .build();

        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }

    public MainResponse getFIWhiteList(String token) {
        try {
            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            /*if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }*/
            List<Whitelist> whitelistById = whitelistRepository.findByFiId(mainLoginResponse.getData().getFi_id());

            return MainResponse.builder()
                    .responseDesc("SUCCESS")
                    .responseCode("0")
                    .payload(whitelistById)
                    .build();

        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }

    public MainResponse getSingleWhiteListByPhone(String phone, Integer productId, String token) {
        try {
            MainLoginResponse mainLoginResponse = sendInterestPaymentUtil.whitelistLogin(token);
            if (Objects.isNull(mainLoginResponse)
                    || Objects.isNull(mainLoginResponse.getData())
                    || Objects.isNull(mainLoginResponse.getData().getRole())
                    || Objects.isNull(mainLoginResponse.getData().getFi_id())
                    || Objects.isNull(mainLoginResponse.getData().getUser_id())) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }
            /*if (!mainLoginResponse.getData().getRole().equalsIgnoreCase("CHECKER")) {
                return MainResponse.builder()
                        .responseDesc("Unauthorized.")
                        .responseCode("1")
                        .build();
            }*/

            Whitelist whitelistById = whitelistRepository.findByPhoneAndProducts_Id(phone, productId);

            if (Objects.isNull(whitelistById)) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }
            if (Objects.nonNull(whitelistById.getStatus()) && whitelistById.getStatus() != ChargeState.ACTIVE) {
                return MainResponse.builder()
                        .responseDesc("Whitelist can't be found")
                        .responseCode("1")
                        .build();
            }

            return MainResponse.builder()
                    .responseDesc("Customer whitelisted.")
                    .responseCode("0")
                    .build();

        } catch (Exception e) {

            log.error(e.getMessage());

            return MainResponse.builder()
                    .responseDesc("Unexpected error occur while parsing the request.")
                    .responseCode("3")
                    .build();
        }

    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                String value = rowScanner.next();
                values.add(value);
            }
        }
        return values;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile("[+]?2(33|51)[0-9]{9}");
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}
