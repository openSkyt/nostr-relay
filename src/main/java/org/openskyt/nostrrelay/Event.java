package org.openskyt.nostrrelay;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Event {

    @Id
    @GeneratedValue
    private long fakeId;
    private String content;

    public Event(String content) {
        this.content = content;
    }
}
