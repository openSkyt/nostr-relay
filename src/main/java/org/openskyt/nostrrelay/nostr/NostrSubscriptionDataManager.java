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

    // relay's current subscriptions data
    private final Set<Subscription> subscriptions = new HashSet<>();

    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    public void closeSub(CloseData closeData) {
        if (subscriptions.contains(closeData.subscription())) {
            removeSubscription(closeData.subscription());
            System.out.println("Current subscription closed");
        }
    }

    public Set<Subscription> getAllSubscriptions() {
        return this.subscriptions;
    }
}