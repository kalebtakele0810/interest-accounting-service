package et.kacha.interestcalculating.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
public class MainRequest implements Serializable {

    @JsonProperty("RequestRefId")
    public String id;

    @JsonProperty("CommandId")
    public String commandId;

    @JsonProperty("Payload")
    public Object payload;

}