package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class serves as a repo manager - we can work out db solutions while not breaking another code
 */
@Component
@RequiredArgsConstructor
public class NostrPersistence {

    private final EventRepository repo;

    public void save(Event event) {
        repo.save(event);
    }

    public EventData retrieveEvent(String id) {
        Optional<Event> optEvent = repo.findById(id);
        return optEvent.map(EventData::new).orElse(null);
    }

    public void delete(Event event) {
        try {
            repo.deleteById(event.getId());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public Optional<Event> retrieveMetaData(String pubkey) {
        return repo.findByPubkeyAndByKind(pubkey, 0).stream().findAny();
    }

    /**
     * Retrieves all valid EVENT-data as required by new subs reqDataSet
     * @param reqDataSet
     * ReqDataSet of a newly created subscription
     * @return
     * Set of valid EventData (to send to the client)
     */
    public Set<Event> getAllEvents(Set<ReqData> reqDataSet) {
        Set<Event> events = new HashSet<>();

        // find all matching events in the DB
        reqDataSet.forEach(request -> events.addAll(repo.findAllMatchingData(
                    request.getAuthors() == null ? new HashSet<>() : request.getAuthors(),
                    request.getKinds() == null ? new HashSet<>() : request.getKinds()
            ))
        );
        return events;
    }
}
