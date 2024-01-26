package et.kacha.interestcalculating.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
public class WebHookRequest implements Serializable {

    @JsonProperty("id")
    public String id;

    @JsonProperty("amount")
    public int amount;

    @JsonProperty("currency")
    public String currency;

    @JsonProperty("created_at_time")
    public int created_at_time;

    @JsonProperty("timestamp")
    public int timestamp;

    @JsonProperty("cause")
    public String cause;

    @JsonProperty("full_name")
    public String full_name;

    @JsonProperty("account_name")
    public String account_name;

    @JsonProperty("invoice_url")
    public String invoice_url;
}