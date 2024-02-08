package org.openskyt.nostrrelay.service;

import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionService {

    private final Set<Subscription> activeSubscriptions = new HashSet<>();

    public Set<Subscription> getAllSubscriptions() {
        return activeSubscriptions;
    }

    public void createNewSubscription(Subscription subscription) {
        activeSubscriptions.add(subscription);
    }

    public void removeSubscription(CloseData closeData) {
        for (Subscription s : activeSubscriptions) {
            if (s.subscription_id().equals(closeData.subscription_id())
                    && s.session().equals(closeData.session())) {
                activeSubscriptions.remove(s);
                return;
            }
        }
    }

    public void removeSubscription(WebSocketSession session) {
        activeSubscriptions.removeIf(sub -> sub.session().equals(session));
    }
}