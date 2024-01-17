package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.openskyt.nostrrelay.dto.Subscription;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles REQ and CLOSE data, manages subscriptions, sends subscription feed
 */
@Component
@RequiredArgsConstructor
public class NostrSubscriptionHandler {

    private final NostrUtil util = new NostrUtil();
    private final NostrPersistence persistence;
    private final Map<Subscription, Set<ReqData>> subs = new HashMap<>();

    /**
     * Handles REQ-message by adding a new subscription to subs then sends EVENT feed for a new subscription back.
     * @param reqDataSet
     * parsed REQ-Message data SET
     */
    public void handleReq(Set<ReqData> reqDataSet) { // one REQ message may contain more than one ReqData (filter)
        Subscription subscription = reqDataSet.iterator().next().getSubscription();
        if (subs.containsKey(subscription)) {
            subs.put(subscription, reqDataSet);
        }
        handleNewSubFeed(reqDataSet);
    }

    /**
     * Handles NOSTR CLOSE-message by closing existing subscription
     * @param closeData
     * parsed CLOSE-message data (present actual subscription)
     */
    public void handleClose(CloseData closeData) {
        if (subs.containsKey(closeData.subscription())) {
            subs.remove(closeData.subscription());
            System.out.println("subscription removed!");
        }
    }

    /**
     * Feeds newly created subscription with REQuested retrieved existing EVENT-data
     * @param reqDataSet
     * incoming REQ-data SET
     */
    private void handleNewSubFeed(Set<ReqData> reqDataSet) {
        Set<EventData> validEventData = new HashSet<>();
        // filter db-events with filterSubFeed()
        for (EventData eventData : persistence.retrieveAllEvents()) {
            validEventData.addAll(filterSubFeed(eventData, reqDataSet));
        }
        // send feed and single eose for subscription
        sendSubFeed(validEventData);
        WebSocketSession session = reqDataSet.iterator().next().getSubscription().session();
        try {
            session.sendMessage(util.eoseMessage(reqDataSet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Feeds current subscriptions with REQuested incoming EVENT-data
     * @param eventData
     * examined EVENT-data to filter
     */
    public void handleSubFeed(EventData eventData) {
        Set<EventData> validEventData = new HashSet<>();
        // filter incoming event by each ReqData in Map<Subscription, Set<ReqData>> - for each sub individually
        for (Map.Entry<Subscription, Set<ReqData>> entry : subs.entrySet()) {
            Set<ReqData> reqDataSet = entry.getValue();
            validEventData.addAll(filterSubFeed(eventData, reqDataSet));
        }
        sendSubFeed(validEventData);
    }

    /**
     * Compares EVENT-data to REQ-data specifics. Sets the right subscription data to event after filtering. Note there might be more REQ-data for single subscription. This method is meant to be cast inside subscription handling methods. (sub method)
     * @param eventData
     * EVENT-data to examine
     * @param reqDataSet
     * REQ-data SET to filter by
     * @return
     * compatible EVENT-data
     */
    private Set<EventData> filterSubFeed(EventData eventData, Set<ReqData> reqDataSet) {
        Set<EventData> validEventData = new HashSet<>();
        if (reqDataSet == null) return validEventData;
        reqDataSet.forEach(r -> {
            // combine all conditions
            if ((r.getKinds() == null || r.getKinds().isEmpty() || r.getKinds().contains(eventData.getKind()))                  // kinds filter
                    && (r.getAuthors() == null || r.getAuthors().isEmpty() || r.getAuthors().contains(eventData.getPubkey()))   // authors filter
                    && (r.getIds() == null || r.getIds().isEmpty() || r.getIds().contains(eventData.getId()))                   // ids filter
                    && (r.getSince() <= eventData.getCreated_at())                                                              // since filter
                    && (r.getUntil() >= eventData.getCreated_at())) {                                                           // until

                eventData.setSubscription(reqDataSet.iterator().next().getSubscription());
                validEventData.add(eventData); // returning set solves NullPointerException if element is filtered out
            }
        });
        return validEventData;
    }

    /**
     * Sends EVENT-data SET to client (sub method)
     * @param eventDataSet
     * EventData to send to client
     */
    private void sendSubFeed(Set<EventData> eventDataSet) {
        eventDataSet.forEach(eventData -> {
            try {
                eventData.getSubscription().session().sendMessage(util.eventMessage(eventData));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
