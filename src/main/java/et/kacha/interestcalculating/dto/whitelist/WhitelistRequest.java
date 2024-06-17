package et.kacha.interestcalculating.dto.whitelist;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhitelistRequest {

    @JsonProperty("msisdn")
    private String msisdn;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("whitelist_id")
    private Integer whitelistId;

}

