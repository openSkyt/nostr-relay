package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.EventData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles EVENT-data processing logic
 */
@Component
@RequiredArgsConstructor
public class NostrEventHandler {

    private final NostrPersistence persistence;
    private final NostrUtil util;

    /**
     * Handles EVENT by distributing EVENT-data to a proper logic-handling method defined by EVENT-kind
     * @param eventData
     * parsed EVENT-message data
     */
    public void handleEvent(EventData eventData) {
        switch (eventData.getKind()) {
            case 0      : handleEvent_0(eventData); break;
            case 1      : handleEvent_1(eventData); break;
            default     :
                try {
                    eventData.getSubscription().session().sendMessage(util.noticeMessage("Unknown event kind"));
                } catch (IOException e) {
                    System.out.println("Unknown event kind!");
                    throw new RuntimeException(e);
                }
        }
    }

    /**
     * Handles EVENT-kind 0 by checking redundancy, saves to DB. Responds with OK-message
     * @param eventData
     * incoming EVENT-data
     */
    private void handleEvent_0(EventData eventData) {
        Optional<EventData> optEventData = persistence.findByPubkey(eventData.getPubkey());
        // remove redundant data, send NOTICE message
        if (optEventData.isPresent()) {
            persistence.delete(optEventData.get());
            try {
                eventData.getSubscription().session().sendMessage(util.noticeMessage("metadata updated"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // save new data, send OK message
        persistence.saveEvent(eventData);
        try {
            eventData.getSubscription().session().sendMessage(util.okMessage(eventData, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles EVENT-kind 1 by saving into the DB. Responds with OK-message.
     * @param eventData
     * incoming EVENT-data
     */
    private void handleEvent_1(EventData eventData) {
        try {
            persistence.saveEvent(eventData);
            eventData.getSubscription().session().sendMessage(util.okMessage(eventData, true));
        } catch (Exception e) {
            System.err.println("Session closed");
        }
    }

}
