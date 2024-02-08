package org.openskyt.nostrrelay.nostr;

import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.NostrConsumer;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionManager implements NostrConsumer {

    private final Set<Subscription> activeSubscriptions = new HashSet<>();

    public SubscriptionManager(ReqObserver reqObserver, CloseObserver closeObserver, SessionObserver sessionObserver) {
        reqObserver.subscribe(this);
        closeObserver.subscribe(this);
        sessionObserver.subscribe(this);
    }

    public Set<Subscription> getAllSubscriptions() {
        return activeSubscriptions;
    }

    @Override
    public void handle(Object o) {
        if (o instanceof Subscription) {
            createNewSubscription((Subscription) o);
        } else if (o instanceof CloseData) {
            removeSubscription((CloseData) o);
        } else if (o instanceof WebSocketSession) {
            removeSubscription((WebSocketSession) o);
        }
    }

    private void createNewSubscription(Subscription subscription) {
        activeSubscriptions.add(subscription);
    }

    private void removeSubscription(CloseData closeData) {
        for (Subscription s : activeSubscriptions) {
            if (s.subscription_id().equals(closeData.subscription_id())
                    && s.session().equals(closeData.session())) {
                activeSubscriptions.remove(s);
                return;
            }
        }
    }

    private void removeSubscription(WebSocketSession session) {
        activeSubscriptions.removeIf(sub -> sub.session().equals(session));
    }
}