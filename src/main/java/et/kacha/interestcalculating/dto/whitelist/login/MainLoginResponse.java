package et.kacha.interestcalculating.dto.whitelist.login;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MainLoginResponse {

    @JsonProperty("status_code")
    public Integer status_code;

    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("data")
    public Data data;

}

