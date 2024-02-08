package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.BIP340_Schnorr.EventSigValidator;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Component
public class NostrSubscriptionDataManager implements NostrConsumer {

    private final Set<Subscription> subscriptions = new HashSet<>();
    private final Logger logger = LoggerFactory.getLogger(NostrSubscriptionDataManager.class);

    public NostrSubscriptionDataManager(SessionObserver observer) {
        observer.subscribe(this);
    }

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
        logger.info("new subscription created: " + subscription.subscription_id());
    }

    public void closeSub(CloseData closeData) {
        for (Subscription s : subscriptions) {
            if (closeData.session().equals(s.session()) && closeData.subscription_id().equals(s.subscription_id())) {
                subscriptions.remove(s);
                return;
            }
        }
    }

    public void removeSubscription(WebSocketSession session) {
        subscriptions.removeIf(sub -> sub.session().equals(session));
        logger.info("current sub size: " + subscriptions.size());
    }

    public Set<Subscription> getAllSubscriptions() {
        return this.subscriptions;
    }

    @Override
    public void handle(Object o) {
        if (o instanceof WebSocketSession) {
            removeSubscription((WebSocketSession) o);
        }
    }
}