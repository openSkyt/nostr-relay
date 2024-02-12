package org.openskyt.nostrrelay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.util.NostrUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionManager subscriptionManager;
    private final EventService eventService;
    private final NostrUtil util;

    /**
     * Feeds newly created subscription with REQuested retrieved persisted EVENT-data
     */
    public void sendPersistedData(Subscription subscription) {
        Set<Event> events = eventService.getAllEvents(subscription.filters());
        sendEvents(subscription, events);
        try {
            subscription.session().sendMessage(util.eoseMessage(subscription));
        } catch (IOException e) {
            log.warn("Failed to send REQuested EVENT-data", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Feeds current subscriptions with REQuested incoming EVENT-data
     * @param event examined EVENT-data to filter
     */
    public void handleNewEvent(Event event) {
        for (Subscription subscription : subscriptionManager.getAllSubscriptions()) {
            if (doesMatch(event, subscription.filters())) {
                sendEvents(subscription, Set.of(event));
            }
        }
    }

    /**
     * Compares EVENT-data to REQ-data specifics. Note there might be more REQ-data for a single subscription.
     * @param event         EVENT-data to examine
     * @param reqFilterSet  REQ-data SET to filter by
     * @return true if the event matches any of the filters, false otherwise
     */
    private boolean doesMatch(Event event, Set<ReqFilter> reqFilterSet) {
        if (reqFilterSet == null) {
            return false;
        }

        for (ReqFilter r : reqFilterSet) {
            if (
                    (r.getKinds() == null || r.getKinds().isEmpty() || r.getKinds().contains(event.getKind()))
                            && (r.getAuthors() == null || r.getAuthors().isEmpty() || r.getAuthors().contains(event.getPubkey()))
                            && (r.getIds() == null || r.getIds().isEmpty() || r.getIds().contains(event.getId()))
                            && (r.getSince() == null || r.getSince() <= event.getCreated_at())
                            && (r.getUntil() == null || r.getUntil() >= event.getCreated_at())
                            && (r.getE() == null || r.getE().isEmpty() || containsTags(event.getTags(), "e", r.getE()))
                            && (r.getP() == null || r.getP().isEmpty() || containsTags(event.getTags(), "p", r.getP()))
                            && (r.getT() == null || r.getT().isEmpty() || containsTags(event.getTags(), "t", r.getT()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given list of tags contains all tags with the specified key and values.
     * @param tags     List of tags to check
     * @param key      Tag key
     * @param values   Tag values to match
     * @return true if the list contains a tag with the specified key and values, false otherwise
     */
    private boolean containsTags(String[][] tags, String key, Set<String> values) {
        if (tags == null || tags.length == 0 || key == null || key.isEmpty() || values == null || values.isEmpty()) {
            return true; // If any of the parameters are null or empty, consider it a match
        }
        for (String[] tag : tags) {
            if (tag.length >= 2 && !key.equals(tag[0]) && values.contains(tag[1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sends parsed EVENT-data SET to client (sub method)
     *
     */
    private void sendEvents(Subscription subscription, Set<Event> events) {
        events.forEach(event -> {
            try {
                subscription.session().sendMessage(util.stringifyEvent(event, subscription));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });
    }

}
