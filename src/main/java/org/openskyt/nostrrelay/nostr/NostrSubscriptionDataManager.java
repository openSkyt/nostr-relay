package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class NostrSubscriptionDataManager {

    // relay's current subscriptions data
    private final Map<Subscription, Set<ReqData>> subscriptions = new HashMap<>();

    public void addSubscription(Subscription subscription, Set<ReqData> reqDataSet) {
        subscriptions.put(subscription, reqDataSet);
    }

    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
    }

    public void closeSub(CloseData closeData) {
        if (subscriptions.containsKey(closeData.subscription())) {
            removeSubscription(closeData.subscription());
            System.out.println("Current subscription closed");
        }
    }

    public Map<Subscription, Set<ReqData>> getAllSubs() {
        return this.subscriptions;
    }
}

