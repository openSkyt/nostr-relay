package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NostrPersistence {

    private final EventRepository repo;
    private final MongoTemplate mongoTemplate;

    public void save(Event event) {
        repo.save(event);
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

    public Set<Event> getAllEvents(Set<ReqFilter> reqFilterSet) {
        Set<Event> events = new HashSet<>();

        reqFilterSet.forEach(request -> {
            Criteria criteria = new Criteria();

            if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
                criteria.and("pubkey").in(request.getAuthors());
            }

            if (request.getKinds() != null && !request.getKinds().isEmpty()) {
                criteria.and("kind").in(request.getKinds());
            }

            if (request.getSince() != null) {
                criteria.and("created_at").gte(request.getSince());
            }

            if (request.getUntil() != null) {
                criteria.and("created_at").lte(request.getUntil());
            }

            Query query = new BasicQuery(criteria.getCriteriaObject());
            events.addAll(mongoTemplate.find(query, Event.class));
        });

        return events;
    }
}
