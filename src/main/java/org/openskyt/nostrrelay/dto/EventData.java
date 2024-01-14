package org.openskyt.nostrrelay.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openskyt.nostrrelay.model.Event;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
@NoArgsConstructor
public class EventData {

    private String id;
    private String pubkey;
    private long created_at;
    private int kind;
    private String tags;
    private String content;
    private String sig;

    @JsonIgnore
    private WebSocketSession session;

    public EventData(Event event) {
        this.id = event.getId();
        this.pubkey = event.getPubkey();
        this.created_at = event.getCreated_at();
        this.kind = event.getKind();
        this.tags = event.getTags();
        this.content = event.getContent();
        this.sig = event.getSig();
    }
}
