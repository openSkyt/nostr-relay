package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.springframework.stereotype.Component;

/** Handles REQ related logic, invokes SubscriptionFeeder.handleNewSubFeed() in order to send requested EVENT-data to client.
 *
 */
@Component
public class NostrReqHandler implements NostrConsumer {

    private final NostrSubscriptionDataManager subscriptionDataManager;

    public NostrReqHandler(ReqObserver observer, NostrSubscriptionDataManager subscriptionDataManager) {
        this.subscriptionDataManager = subscriptionDataManager;

        observer.subscribe(this);
    }

    /**
     * Handles REQ-message by adding a new subscription to subs then sends EVENT feed for a new subscription back.
     */
    private void handle(Subscription subscription) { // single REQ message may contain multiple ReqData (filter)
        subscriptionDataManager.addSubscription(subscription);
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof Subscription) {
            handle((Subscription) o);
        }
    }
}
