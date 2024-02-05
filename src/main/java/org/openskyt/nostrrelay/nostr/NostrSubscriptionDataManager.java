package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class NostrSubscriptionDataManager {

    private final Set<Subscription> subscriptions = new HashSet<>();

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void closeSub(CloseData closeData) {
        for (Subscription s : subscriptions) {
            if (closeData.session().equals(s.session()) && closeData.subscription_id().equals(s.subscription_id())) {
                subscriptions.remove(s);
                return;
            }
        }
    }

    public Set<Subscription> getAllSubscriptions() {
        return this.subscriptions;
    }
}