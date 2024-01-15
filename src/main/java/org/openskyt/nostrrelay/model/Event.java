package org.openskyt.nostrrelay.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;

@Getter
@NoArgsConstructor
public class Event {

    @Id
    private String id;
    private String pubkey;
    private long created_at;
    private int kind;
    private String[][] tags;
    private String content;
    private String sig;

    public Event(EventData eventData) {
        this.id = eventData.getId();
        this.pubkey = eventData.getPubkey();
        this.created_at = eventData.getCreated_at();
        this.kind = eventData.getKind();
        this.tags = eventData.getTags();
        this.content = eventData.getContent();
        this.sig = eventData.getSig();
    }

    public Event(String id, String pubkey, long created_at, int kind, String[][] tags, String content, String sig) {
        this.id = id;
        this.pubkey = pubkey;
        this.created_at = created_at;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.sig = sig;
    }
}
