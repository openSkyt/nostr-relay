package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.ReqData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Handles REQ related logic, invokes SubscriptionFeeder.handleNewSubFeed() in order to send requested EVENT-data to client.
 *
 */
@Component
public class NostrReqHandler implements NostrConsumer {

    private final ReqObserver observer;
    private final NostrSubscriptionDataManager subscriptionDataManager;
    private final NostrSubscriptionFeeder subscriptionFeeder;

    public NostrReqHandler(ReqObserver observer, NostrSubscriptionDataManager subscriptionDataManager, NostrSubscriptionFeeder subscriptionFeeder) {
        this.observer = observer;
        this.subscriptionDataManager = subscriptionDataManager;
        this.subscriptionFeeder = subscriptionFeeder;

        observer.subscribe(this);
    }

    /**
     * Handles REQ-message by adding a new subscription to subs then sends EVENT feed for a new subscription back.
     * @param reqDataSet
     * parsed REQ-Message data SET
     */
    private void handle(Set<ReqData> reqDataSet) { // single REQ message may contain multiple ReqData (filter)
        System.out.println("NostrReqHandler.handle(Set<ReqData>) - actual impl. invoked");
        Subscription subscription = reqDataSet.iterator().next().getSubscription();
        subscriptionDataManager.addSubscription(subscription, reqDataSet);
        subscriptionFeeder.handleNewSubFeed(reqDataSet);
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class
    @Override
    public void handle(Object o) {
        if (o instanceof Set<?>) {
            handle((Set<ReqData>) o);
        }
    }
}
