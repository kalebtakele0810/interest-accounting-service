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
public class Data {

    @JsonProperty("role")
    public String role;

    @JsonProperty("user_id")
    public int user_id;

    @JsonProperty("fi_id")
    public int fi_id;

}

