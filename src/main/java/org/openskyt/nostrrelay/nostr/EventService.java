package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles EVENT-data processing logic
 */
@Component
public class EventService implements NostrConsumer {
    private final NostrPersistence persistence;
    private final NostrSubscriptionFeeder subscriptionFeeder;

    public EventService(EventObserver observer, NostrPersistence persistence, NostrSubscriptionFeeder subscriptionFeeder) {
        this.persistence = persistence;
        this.subscriptionFeeder = subscriptionFeeder;

        observer.subscribe(this);
    }

    /**
     * Handles EVENT by delegating EVENT-data to a proper logic-handling method defined by EVENT-kind
     * @param event
     * parsed EVENT-message data
     */
    private void handle(Event event) {
        switch (event.getKind()) {
            case 0      : handleEvent_0(event); break;
            case 1      : handleEvent_1(event); break;
            default     : System.out.println("ignoring event: " + event);
        }
        subscriptionFeeder.handleNewEvent(event); // handle event -> then broadcast :)
    }

    /**
     * Handles EVENT-kind 0 by checking redundancy, saves to DB. Responds with OK-message
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent_0(Event event) {
        Optional<Event> optEventData = persistence.retrieveMetaData(event.getPubkey());
        // remove redundant data
        optEventData.ifPresent(persistence::delete);

        persistence.save(event);
    }

    /**
     * Handles EVENT-kind 1 by saving into the DB. Responds with OK-message.
     * @param event
     * incoming EVENT-data
     */
    private void handleEvent_1(Event event) {
        persistence.save(event);
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof Event) {
            handle((Event) o);
        }
    }
}
