package org.openskyt.nostrrelay.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

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
    @Indexed(expireAfterSeconds = 0)
    private Date expirationTime;

    public void setExpirationTimeIfExists() {
        this.expirationTime = getExpirationValue(tags);
    }

    public Event(String id, String pubkey, long created_at, int kind, String[][] tags, String content, String sig) {//todo remove this constructor is used only for testing

        this.id = id;
        this.pubkey = pubkey;
        this.created_at = created_at;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.sig = sig;
    }

    private Date getExpirationValue(String[][] tags) {
        for (String[] tag : tags) {
            if (tag.length > 1 && "expiration".equals(tag[0])) {
                System.out.println();
                return new Date(Long.parseLong(tag[1]) * 1000);
            }
        }
        return null;
    }
}