package org.openskyt.nostrrelay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class EventService {

    private final EventRepository repo;
    private final MongoTemplate mongoTemplate;

    public void save(Event event) {
        repo.save(event);
    }

    public void delete(Event event) {
        try {
            repo.deleteById(event.getId());
            log.info("Deleted event with id: " + event.getId());

        } catch (Exception e) {
            log.warn("Failed to delete event with id: " + event.getId());
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

            if (request.getKinds() != null && !request.getKinds().isEmpty()) {
                criteria.and("kind").in(request.getKinds());
            }

            if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
                criteria.and("pubkey").in(request.getAuthors());
            }

            if (request.getIds() != null && !request.getIds().isEmpty()) {
                criteria.and("id").in(request.getIds());
            }

            if (request.getSince() != null) {
                criteria.and("created_at").gte(request.getSince());
            }
            if (request.getUntil() != null) {
                criteria.and("created_at").lte(request.getUntil());
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
    public boolean exists(String id) {
        return repo.existsById(id);
    }

    public Optional<Event> getFollowList(String pubkey) {
        return repo.findByPubkeyAndByKind(pubkey, 3).stream().findAny();
    }

    public void deleteEvents(Event event) {
        String[][] tags = event.getTags();
        Criteria criteria = new Criteria();
        Arrays.stream(tags).forEach(t -> {
            if (t[0].equals("e")) {
                criteria.and("id").is(t[1]);
                criteria.and("pubkey").is(event.getPubkey());
            } else if (t[0].equals("a")) {

            }
            Query query = new Query(criteria);
            mongoTemplate.remove(query, Event.class);
        });
    }
}