package org.openskyt.nostrrelay.repository;

import io.micrometer.observation.ObservationFilter;
import org.openskyt.nostrrelay.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{'id': ?0}")
    Optional<Event> findById(String id);
    @Query("{'pubkey': ?0}")
    List<Event> findByPubkey(String pubkey);
    @Query("{'pubkey': ?0, 'kind': ?1}")
    List<Event> findByPubkeyAndByKind(String pubkey, int kind);
}
//https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-windows/
