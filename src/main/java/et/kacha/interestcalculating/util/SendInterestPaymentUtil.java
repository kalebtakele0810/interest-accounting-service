package et.kacha.interestcalculating.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import et.kacha.interestcalculating.dto.MainInterestRequest;
import et.kacha.interestcalculating.dto.whitelist.login.MainLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class SendInterestPaymentUtil {

    @Value("${webservice.endpoint.url}")
    private String webServiceEndpoint;

    @Value("${webservice.whitelist.login.url}")
    private String webServiceLoginEndpoint;

    @Value("${webservice.ussd.endpoint.url}")
    private String webServiceUSSDEndpoint;

    @Value("${webservice.endpoint.api-key}")
    private String webServiceApiKey;

    public String sendPaymentRequest(MainInterestRequest paymentRequest) {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(trivialHostnameVerifier);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("API-Key", webServiceApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<MainInterestRequest> requestEntity = new HttpEntity<>(paymentRequest, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(webServiceEndpoint, requestEntity, String.class);

            log.info("Pay interest response from webservice:" + (Objects.nonNull(responseEntity.getBody()) ? responseEntity.getBody() : ""));

            if (Objects.nonNull(responseEntity.getBody()) && findWord(responseEntity.getBody(), "SUCCESS")) {
                return responseEntity.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String sendUSSDRequest(List<Integer> ussdRequest) {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(trivialHostnameVerifier);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("API-Key", webServiceApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(ussdRequest, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(webServiceUSSDEndpoint, requestEntity, String.class);

            log.info("USSD response from webservice:" + (Objects.nonNull(responseEntity.getBody()) ? responseEntity.getBody() : ""));

            if (Objects.nonNull(responseEntity.getBody()) && findWord(responseEntity.getBody(), "SUCCESS")) {
                return responseEntity.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public MainLoginResponse whitelistLogin(String token) {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(trivialHostnameVerifier);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("API-Key", webServiceApiKey);
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<MainLoginResponse> responseEntity = restTemplate.postForEntity(webServiceLoginEndpoint, requestEntity, MainLoginResponse.class);

            log.info("Whitelist login response from webservice:" + (Objects.nonNull(responseEntity.getBody()) ? new ObjectMapper().writeValueAsString(responseEntity.getBody()) : ""));

            if (Objects.nonNull(responseEntity.getBody()) && responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final HostnameVerifier trivialHostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession sslSession) {
            return true;
        }
    };
    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};

    private boolean findWord(String response, String stringToFind) {
        // Regular expression pattern to match the word
        String pattern = "\\b" + Pattern.quote(stringToFind) + "\\b";

        // Create a Pattern object
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

        // Create a Matcher object
        Matcher m = p.matcher(response);

        // Check if the word is found in the string
        return m.find();
    }

}
