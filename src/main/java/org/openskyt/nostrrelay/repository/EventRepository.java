package org.openskyt.nostrrelay.repository;

import org.openskyt.nostrrelay.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{'pubkey': ?0}")
    List<Event> findByPubkey(String pubkey);
    @Query("{'pubkey': ?0, 'kind': ?1}")
    List<Event> findByPubkeyAndByKind(String pubkey, int kind);

}
