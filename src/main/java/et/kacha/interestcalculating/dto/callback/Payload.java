package et.kacha.interestcalculating.dto.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Payload implements Serializable {

    @JsonProperty("fi_id")
    public String fi_id;

    @JsonProperty("responseCode")
    public int responseCode;

    @JsonProperty("responseDesc")
    public String responseDesc;

    @JsonProperty("RequestRefId")
    public String requestRefId;

    @JsonProperty("txn_ref")
    public String txn_ref;

}