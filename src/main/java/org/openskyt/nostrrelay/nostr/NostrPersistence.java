package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@EnableScheduling
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

    public Optional<Event> getMetaData(String pubkey) {
        return repo.findByPubkeyAndByKind(pubkey, 0).stream().findAny();
    }

    /**
     * Retrieves requested data based on request filter sent by client in REQ-message
     * @param reqFilterSet
     * subscription filter-set
     * @return
     * valid persisted event data
     */
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

            if (request.getIds() != null && !request.getIds().isEmpty()) {
                criteria.and("id").in(request.getIds());
            }

            if (request.getE() != null && !request.getE().isEmpty()) {
                criteria.and("tags").elemMatch(
                        Criteria.where("0").is("e").and("1").in(request.getE())
                );
            }
            if (request.getP() != null && !request.getP().isEmpty()) {
                criteria.and("tags").elemMatch(
                        Criteria.where("0").is("p").and("1").in(request.getP())
                );
            }
            if (request.getT() != null && !request.getT().isEmpty()) {
                criteria.and("tags").elemMatch(
                        Criteria.where("0").is("t").and("1").in(request.getT())
                );
            }

            if (request.getSince() != null) {
                criteria.and("created_at").gte(request.getSince());
            }
            if (request.getUntil() != null) {
                criteria.and("created_at").lte(request.getUntil());
            }

            if (request.getLimit() != null) {
                Query limitedQuery = new Query(criteria).limit(request.getLimit());
                events.addAll(mongoTemplate.find(limitedQuery, Event.class));
            } else {
                Query query = new Query(criteria);
                events.addAll(mongoTemplate.find(query, Event.class));
            }
        });
        return events;
    }

    @Scheduled(fixedRate = 600_000) // each 10 minutes
    private void removeDocumentsWithExpiredTimestamp() {
        Query query = Query.query(Criteria.where("tags")
                .elemMatch(Criteria.where("0").is("expiration"))
        );
        List<Event> expiringEvents = mongoTemplate.find(query, Event.class);

        for (Event event : expiringEvents) {
            Long expirationValue = getExpirationValue(event.getTags());
            if (expirationValue != null && expirationValue <= (System.currentTimeMillis() / 1000L)) {
                removeDocument(event.getId());
            }
        }
    }

    private Long getExpirationValue(String[][] tags) {
        for (String[] tag : tags) {
            if (tag.length > 1 && "expiration".equals(tag[0])) {
                return Long.parseLong(tag[1]);
            }
        }
        return null;
    }

    private void removeDocument(String documentId) {
        Query removeQuery = Query.query(Criteria.where("_id").is(documentId));
        mongoTemplate.remove(removeQuery, "event");
    }
}