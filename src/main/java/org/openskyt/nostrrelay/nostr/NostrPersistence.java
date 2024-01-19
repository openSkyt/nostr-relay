package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.EventRepo;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class serves as a repo manager - we can work out db solutions while not breaking another code
 */
@Component
@RequiredArgsConstructor
public class NostrPersistence {

    private final EventRepo repo; //todo replace with mongo
    private final EventRepository eventRepository;


    public void saveEvent(EventData eventData) {
        repo.save(new Event(eventData));
        eventRepository.save(new Event(eventData));
        dbLog(eventData);
    }

    public EventData retrieveEvent(String id) {
        Optional<Event> optEvent = eventRepository.findById(id);

        return optEvent.map(EventData::new).orElse(null);
    }

    public List<EventData> retrieveAllEvents() {
        return eventRepository.findAll().stream()
                .map(EventData::new)
                .collect(Collectors.toList());
    }

    public List<EventData> findByPubkey(String pubkey) {
        return eventRepository.findByPubkey(pubkey).stream().map(EventData::new).collect(Collectors.toList());
    }

    public void delete(EventData eventData) {
        eventRepository.deleteById(eventData.getId());
    }

    public void dbLog(EventData eventData) {
        System.out.println("db: Currently saved events: " + getEventCount());
        if (eventRepository.findById(eventData.getId()).isPresent()) {
            System.out.println("db: last saved event: " + retrieveEvent(eventData.getId()).toString());
        }
    }

    public long getEventCount() {
        return eventRepository.count();
    }

    public List<EventData> findByPubkeyAndByKind(String pubkey, int kind) {
        return eventRepository.findByPubkeyAndByKind(pubkey, kind).stream().map(EventData::new).collect(Collectors.toList());
    }
}
