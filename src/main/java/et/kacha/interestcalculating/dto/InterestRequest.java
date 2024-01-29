package et.kacha.interestcalculating.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterestRequest {
    public InterestRequest() {
        this.msisdn = "";
        this.productId = "";
    }

    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("timestamp")
    private Date timestamp;
}

