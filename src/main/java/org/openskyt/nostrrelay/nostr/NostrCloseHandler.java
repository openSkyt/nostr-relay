package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles CLOSE data related logic, removes subscription held in NostrSubscriptionDataManager subscriptions HashMap
 */
@Component
public class NostrCloseHandler implements NostrConsumer {

    private final NostrSubscriptionDataManager subscriptionDataManager;
    private final NostrUtil util;

    public NostrCloseHandler(CloseObserver closeObserver,
                             NostrSubscriptionDataManager subscriptionDataManager,
                             NostrUtil util) {

        this.subscriptionDataManager = subscriptionDataManager;
        this.util = util;

        closeObserver.subscribe(this); //
    }

    /**
     * Handles valid CLOSE data by closing existing subscription
     * @param closeData
     * parsed CLOSE-message data (present actual subscription)
     */
    private void handle(CloseData closeData) {
        subscriptionDataManager.closeSub(closeData);
        try {
            closeData.session().sendMessage(util.noticeMessage("subscription closed"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // overridden method from implemented NostrConsumer interface - invokes actual impl. defined in this class when action is observed
    @Override
    public void handle(Object o) {
        if (o instanceof CloseData) {
            handle((CloseData) o);
        }
    }
}