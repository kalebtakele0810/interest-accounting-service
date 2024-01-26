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
public class ApiResponse {
    public ApiResponse() {
        this.id = "";
        this.responseCode = "";
        this.responseDesc = "";
    }

    @JsonProperty("id")
    private String id;
    @JsonProperty("ResponseCode")
    private String responseCode;
    @JsonProperty("ResponseDesc")
    private String responseDesc;
}
