package org.openskyt.nostrrelay.repository;

import org.openskyt.nostrrelay.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Set;

public interface EventRepository extends MongoRepository<Event, String> {

    @Query("{'pubkey': ?0, 'kind': ?1}")
    Set<Event> findByPubkeyAndByKind(String pubkey, int kind);

    @Query("{$or: ["
            + "{ $or: [{ 'pubkey': { $exists: false } }, { 'pubkey': { $in: ?0 } }] }, "
            + "{ $or: [{ 'kind': { $exists: false } }, { 'kind': { $in: ?1 } }] },"
            + "{ $or: [{ 'id': { $exists: false } }, { 'id': { $in: ?2 } }] }, "
            + "]}")
    Set<Event> findAllMatchingData(Set<String> authors,
                                   Set<Integer> kinds,
                                   Set<String> ids);
}