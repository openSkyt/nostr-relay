package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
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

    private final EventRepository repo;


    public void save(EventData eventData) {
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

    public void delete(EventData eventData) {
        try {
            System.out.println("second delete called");
            repo.deleteById(eventData.getId());
            System.out.println("after deletion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<EventData> retrieveMetaData(String pubkey) {
        System.out.println("retrieving");
        return repo.findByPubkeyAndByKind(pubkey, 0).stream().findAny().map(EventData::new);
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
}
