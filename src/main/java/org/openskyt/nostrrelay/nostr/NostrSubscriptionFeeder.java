package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * This class handles feeding clients with subscribed data
 */
@Component
public class NostrSubscriptionFeeder implements NostrConsumer {

    private final NostrPersistence persistence;
    private final NostrSubscriptionDataManager subscriptionDataManager;
    private final NostrUtil util;

    public NostrSubscriptionFeeder(NostrPersistence persistence,
                                   NostrSubscriptionDataManager subscriptionDataManager,
                                   NostrUtil util,
                                   EventObserver eventObserver,
                                   ReqObserver reqObserver) {

        this.persistence = persistence;
        this.subscriptionDataManager = subscriptionDataManager;
        this.util = util;
        eventObserver.subscribe(this);
        reqObserver.subscribe(this);
    }

    /**
     * Feeds newly created subscription with REQuested retrieved persisted EVENT-data
     *
     */
    public void sendPersistedData(Subscription subscription) {
        Set<Event> events = persistence.getAllEvents(subscription.filters());
        sendEvents(subscription, events);
        try {
            subscription.session().sendMessage(util.eoseMessage(subscription));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Feeds current subscriptions with REQuested incoming EVENT-data
     * @param event examined EVENT-data to filter
     */
    public void handleNewEvent(Event event) {
        for (Subscription subscription : subscriptionDataManager.getAllSubscriptions()) {
            if (doesMatch(event, subscription.filters())) {
                sendEvents(subscription, Set.of(event));
            }
        }
    }

    /**
     * Compares EVENT-data to REQ-data specifics. Sets the right subscription data to event after filtering. Note there might be more REQ-data for single subscription. This method is meant to be cast inside subscription handling methods. (sub method)
     * @param event  EVENT-data to examine
     * @param reqFilterSet REQ-data SET to filter by
     * @return compatible EVENT-data
     */
    private boolean doesMatch(Event event, Set<ReqFilter> reqFilterSet) {
        if (reqFilterSet == null) {
            return false;
        }

        for (ReqFilter r : reqFilterSet) {
            if ((r.getKinds() == null || r.getKinds().isEmpty() || r.getKinds().contains(event.getKind()))                      // kinds filter
                    && (r.getAuthors() == null || r.getAuthors().isEmpty() || r.getAuthors().contains(event.getPubkey()))) {    // authors filter

                return true;
            }
        }
        return false;
    }

    /**
     * Sends EVENT-data SET to client (sub method)
     *
     */
    private void sendEvents(Subscription subscription, Set<Event> events) {
        events.forEach(event -> {
            try {
                subscription.session().sendMessage(util.stringifyEvent(event, subscription));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });
    }

    @Override
    public void handle(Object o) {
        if (o instanceof Event) {
            handleNewEvent((Event) o);
        } else if (o instanceof Subscription) {
            sendPersistedData((Subscription) o);
        }
    }
}
