package org.openskyt.nostrrelay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
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
    
    @JsonIgnore
    @Indexed(expireAfterSeconds = 0)
    private Date expirationTime;
    @JsonIgnore
    private Integer committedPowLevel;

    // TODO - only for testing - delete afterwards
    public Event(String id, String pubkey, long created_at, int kind, String[][] tags, String content, String sig) {
        this.id = id;
        this.pubkey = pubkey;
        this.created_at = created_at;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.sig = sig;

        this.setExaminedValues();
    }

    public void setExaminedValues() {
        for (String[] tag : this.tags) {
            if (tag.length == 2 && "expiration".equals(tag[0])) {
                this.expirationTime = new Date(Long.parseLong(tag[1]) * 1000);
            }
            if (tag.length == 3 && "nonce".equals(tag[0])) {
                this.committedPowLevel = Integer.parseInt(tag[2]);
            }
        }
    }

    public void addTag(String[] tag) {
        String[][] newTags = new String[this.tags.length + 1][];
        System.arraycopy(this.tags, 0, newTags, 0, this.tags.length);
        newTags[this.tags.length] = tag;
        this.tags = newTags;
    }
}