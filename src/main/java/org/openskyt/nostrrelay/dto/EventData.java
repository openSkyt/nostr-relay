package org.openskyt.nostrrelay.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.openskyt.nostrrelay.model.Event;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EventData {

    private String id;
    private String pubkey;
    private long created_at;
    private int kind;
    private String[][] tags;
    private String content;
    private String sig;

    @JsonIgnore
    private Subscription subscription;

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
