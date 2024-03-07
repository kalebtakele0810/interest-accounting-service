package et.kacha.interestcalculating.dto;

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
public class MainInterestRequest implements Serializable {

    @JsonProperty("fi_id")
    public String fi_id;

    @JsonProperty("CommandId")
    public String commandId;

    @JsonProperty("callback-url")
    public String callbackUrl;

    @JsonProperty("Payload")
    public Object payload;

}