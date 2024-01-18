package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.EventRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class serves as a repo manager - we can work out db solutions while not breaking another code
 */
@Component
@RequiredArgsConstructor
public class NostrPersistence {

    private final EventRepo repo;

    public void saveEvent(EventData eventData) {
        repo.save(new Event(eventData));
        dbLog(eventData);
    }

    public EventData retrieveEvent(String id) {
        Optional<Event> optEvent = repo.findById(id);
        return optEvent.map(EventData::new).orElse(null);
    }

    public List<EventData> retrieveAllEvents() {
        return repo.findAll().stream()
                .map(EventData::new)
                .collect(Collectors.toList());
    }

    public Optional<EventData> findByPubkey(String pubkey) {
        return repo.findByPubkey(pubkey).map(EventData::new);
    }

    public void delete(EventData eventData) {
        repo.deleteById(eventData.getId());
    }

    public void dbLog(EventData eventData) {
        System.out.println("db: Currently saved events: " + getEventCount());
        if (repo.findById(eventData.getId()).isPresent()) {
            System.out.println("db: last saved event: " + retrieveEvent(eventData.getId()).toString());
        }
    }

    public long getEventCount() {
        return repo.count();
    }

    public Optional<EventData> findByPubkeyAndByKind(String pubkey, int kind) {
        return repo.findByPubkeyAndByKind(pubkey, kind).map(EventData::new);
    }
}
