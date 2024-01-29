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
public class InterestResponse {
    public InterestResponse() {
        this.id = "";
        this.msisdn = "";
    }

    @JsonProperty("id")
    private String id;
    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("interest_amount")
    private int interestAmount;
}

