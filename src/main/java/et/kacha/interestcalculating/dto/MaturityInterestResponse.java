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
public class MaturityInterestResponse {

    @JsonProperty("status_code")
    public int statusCode;
    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;
}
