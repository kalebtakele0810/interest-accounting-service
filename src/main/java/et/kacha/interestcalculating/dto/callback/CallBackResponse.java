package et.kacha.interestcalculating.dto.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CallBackResponse implements Serializable {

    @JsonProperty("fi_id")
    public String fi_id;

    @JsonProperty("Payload")
    public ArrayList<Payload> payload;

}