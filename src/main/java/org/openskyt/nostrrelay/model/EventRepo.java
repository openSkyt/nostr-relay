package org.openskyt.nostrrelay.model;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class EventRepo {
    private final Map<String, Event> events = new HashMap<>();

    public Event save(Event e) {
        events.put(e.getId(), e);
        return e;
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
}
