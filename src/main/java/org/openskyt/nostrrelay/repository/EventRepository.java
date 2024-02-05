package org.openskyt.nostrrelay.repository;

import org.openskyt.nostrrelay.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{'pubkey': ?0, 'kind': ?1}")
    Set<Event> findByPubkeyAndByKind(String pubkey, int kind);
}