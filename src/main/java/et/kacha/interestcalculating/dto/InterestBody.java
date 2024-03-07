package et.kacha.interestcalculating.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterestBody {

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("interest_amount")
    private double interestAmount;

    @JsonProperty("tax_amount")
    private double taxAmount;

    @JsonProperty("charge_amount")
    private double chargeAmount;

    @JsonProperty("fi_id")
    private String fiId;

    @JsonProperty("txn_ref")
    private String txnRef;

    @JsonProperty("subscription_id")
    private String subscriptionId;
}

