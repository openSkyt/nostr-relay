package org.openskyt.nostrrelay;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventData {

    private String id;
    private String pubkey;
    private long created_at;
    private int kind;
    private List<String> tags;
    private String content;
    private String sig;
    private WebSocketSession session;
}
