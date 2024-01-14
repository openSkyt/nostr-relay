package org.openskyt.nostrrelay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReqData {

    private List<String> ids;
    private List<String> authors;
    private List<Integer> kinds;
    @JsonProperty("#e")
    private List<String> e;
    @JsonProperty("#p")
    private List<String> p;
    private long since;
    private long until;
    private long limit;
    private WebSocketSession session;
    private String subscription_id;

}


