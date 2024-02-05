package org.openskyt.nostrrelay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Event {

    @Id
    private String id;
    private String pubkey;
    private long created_at;
    private int kind;
    private String[][] tags;
    private String content;
    private String sig;

    @JsonIgnore
    private Long expiration;

    public Event(String id, String pubkey, long created_at, int kind, String[][] tags, String content, String sig) {
        this.id = id;
        this.pubkey = pubkey;
        this.created_at = created_at;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.sig = sig;

        this.expiration = getExpirationValue();
    }

    private Long getExpirationValue() {
        for (String[] tag : tags) {
            if (tag.length > 1 && "expiration".equals(tag[0])) {
                return Long.parseLong(tag[1]);
            }
        }
        return null;
    }
}