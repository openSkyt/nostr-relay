package org.openskyt.nostrrelay.repository;

import org.openskyt.nostrrelay.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Set;

public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{'pubkey': ?0, 'kind': ?1}")
    Set<Event> findByPubkeyAndByKind(String pubkey, int kind);

    @Query("{'pubkey': { $in: ?0 }}")
    Set<Event> findAllMatchingReqData(Set<String> authors);
}
