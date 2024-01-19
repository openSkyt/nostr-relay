package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles EVENT-data processing logic
 */
@Component
public class NostrEventHandler implements NostrConsumer {

    private final EventObserver observer;
    private final NostrPersistence persistence;
    private final NostrUtil util;
    private final NostrSubscriptionFeeder subscriptionFeeder;

    public NostrEventHandler(EventObserver observer, NostrPersistence persistence, NostrUtil util, NostrSubscriptionFeeder subscriptionFeeder) {
        this.observer = observer;
        this.persistence = persistence;
        this.util = util;
        this.subscriptionFeeder = subscriptionFeeder;

        observer.subscribe(this);
    }

    /**
     * Handles EVENT by delegating EVENT-data to a proper logic-handling method defined by EVENT-kind
     * @param eventData
     * parsed EVENT-message data
     */
    private void handle(EventData eventData) {
        switch (eventData.getKind()) {
            case 0      : handleEvent_0(eventData); break;
            case 1      : handleEvent_1(eventData); break;
            default     :
                try {
                    eventData.getSubscription().session().sendMessage(util.noticeMessage("Unknown event kind"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        subscriptionFeeder.handleSubFeed(eventData); // handle event -> then broadcast :)
    }

    /**
     * Handles EVENT-kind 0 by checking redundancy, saves to DB. Responds with OK-message
     * @param eventData
     * incoming EVENT-data
     */
    private void handleEvent_0(EventData eventData) {
        Optional<EventData> optEventData = persistence.retrieveMetaData(eventData.getPubkey());
        // remove redundant data, send OK message with additional payload
        if (optEventData.isPresent()) {
            System.out.println("delete called");
            persistence.delete(optEventData.get());
            persistence.save(eventData);
            try {
                eventData.getSubscription().session().sendMessage(util.okMessage(eventData,true,"metadata updated"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // save new data, send OK message
        persistence.save(eventData);
        try {
            eventData.getSubscription().session().sendMessage(util.okMessage(eventData, true, ""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles EVENT-kind 1 by saving into the DB. Responds with OK-message.
     * @param eventData
     * incoming EVENT-data
     */
    private void handleEvent_1(EventData eventData) {
        try {
            persistence.save(eventData);
            eventData.getSubscription().session().sendMessage(util.okMessage(eventData, true, ""));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof EventData) {
            handle((EventData) o);
        }
    }
}
