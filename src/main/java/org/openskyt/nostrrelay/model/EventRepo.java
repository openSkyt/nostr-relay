package org.openskyt.nostrrelay.model;

import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.*;

@Repository
public class EventRepo {
    private final Map<String, Event> events = new HashMap<>();

    public Event save(Event e) {
        events.put(e.getId(), e);
        return e;
    }

    public void deleteById(String id) {
        events.remove(id);
    }

    public Optional<Event> findById(String id) {
        return Optional.of(events.get(id));
    }

    public Collection<Event> findAll() {
        return events.values();
    }

    public int count() {
        return events.size();
    }

    public Optional<Event> findByPubkey(String pubkey) {
        for (Map.Entry<String, Event> e : events.entrySet()) {
            if (e.getValue().getPubkey().equals(pubkey)) return Optional.of(e.getValue());
        }
        return Optional.empty();
    }

    public Optional<Event> findByPubkeyAndByKind(String pubkey, int kind) {
        for (Map.Entry<String, Event> e : events.entrySet()) {
            if (e.getValue().getPubkey().equals(pubkey)
                    && e.getValue().getKind() == kind) return Optional.of(e.getValue());
        }
        return Optional.empty();
    }
}
