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

    public void save(EventData eventData) {
        repo.save(new Event(eventData));
        dbLog(eventData);
    }

    public EventData retrieveEvent(String id) {
        Optional<Event> optEvent = repo.findById(id);
        return optEvent.map(EventData::new).orElse(null);
    }

    public void delete(EventData eventData) {
        try {
            repo.deleteById(eventData.getId());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public Optional<EventData> retrieveMetaData(String pubkey) {
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

    /**
     * Retrieves all valid EVENT-data as required by new subs reqDataSet
     * @param reqDataSet
     * ReqDataSet of a newly created subscription
     * @return
     * Set of valid EventData (to send to the client)
     */
    public Set<EventData> getNewSubscriptionRequestedData(Set<ReqData> reqDataSet) {
        Set<EventData> validDataSet = new HashSet<>();

        // find all matching events in the DB
        reqDataSet.forEach(request -> {
            Set<Event> events = repo.findAllMatchingData(
                    request.getAuthors() == null ? new HashSet<>() : request.getAuthors(),
                    request.getKinds() == null ? new HashSet<>() : request.getKinds()
            );

            // convert to EventData dto and add subscription info
            Set<EventData> eventDataSet = events.stream()
                    .map(EventData::new)
                    .collect(Collectors.toSet());
            eventDataSet.forEach(e -> e.setSubscription(request.getSubscription()));

            validDataSet.addAll(eventDataSet);
        });
        return validDataSet;
    }
}
