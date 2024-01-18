package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.springframework.stereotype.Component;

/**
 * Handles CLOSE data related logic, removes subscription held in NostrSubscriptionDataManager subscriptions HashMap
 */
@Component
public class NostrCloseHandler implements NostrConsumer {

    private final CloseObserver closeObserver;
    private final NostrSubscriptionDataManager subscriptionDataManager;

    public NostrCloseHandler(CloseObserver closeObserver,
                             NostrSubscriptionDataManager subscriptionDataManager) {

        this.closeObserver = closeObserver;
        this.subscriptionDataManager = subscriptionDataManager;

        closeObserver.subscribe(this); //
    }

    /**
     * Handles valid CLOSE data by closing existing subscription
     * @param closeData
     * parsed CLOSE-message data (present actual subscription)
     */
    private void handle(CloseData closeData) {
        subscriptionDataManager.closeSub(closeData);
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class when action is observed
    @Override
    public void handle(Object o) {
        if (o instanceof CloseData) {
            handle((CloseData) o);
        }
    }
}
