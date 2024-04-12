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
public class CallBackResponse implements Serializable {

    @JsonProperty("reference")
    public String reference;

    @JsonProperty("status")
    public String status;

    @JsonProperty("trace_number")
    public String trace_number;


   /* @JsonProperty("Payload")
    public ArrayList<Payload> payload;*/

}