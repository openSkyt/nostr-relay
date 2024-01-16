package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openskyt.nostrrelay.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NostrController extends TextWebSocketHandler {

    private final NostrPersistence persistence;
    private final NostrDeserializer deser = new NostrDeserializer();
    private final Map<Subscription, Set<ReqData>> subs = new HashMap<>();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull org.springframework.web.socket.CloseStatus status) {

        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        String payload = message.getPayload();
        handleMessage(session, payload);
    }

    /**
     * Handles incoming generic message by checking NOSTR compatible types and invoking proper handling logic.
     * @param session
     * current ws-session
     * @param messageJSON
     * incoming message payload
     */
    private void handleMessage(WebSocketSession session, String messageJSON) {
        try {
            Object[] message = deser.getMapper().readValue(messageJSON, Object[].class);
            switch (message[0].toString()) {
                case "REQ"      : Set<ReqData> reqDataSet = deser.deserializeReqMessage(session, messageJSON);
                                  handleReq(reqDataSet); // handling method
                                  break;
                case "CLOSE"    : CloseData closeData = deser.deserializeCloseMessage(session, messageJSON);
                                  handleClose(closeData); // handling method
                                  break;
                case "EVENT"    : EventData eventData = deser.deserializeEventMessage(session, messageJSON);
                                  handleEvent(eventData); // handling method
                                  handleSubFeed(eventData); // after receiving event -> broadcast
                                  break;
                default         : session.sendMessage(noticeMessage("invalid NOSTR message"));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // HANDLING METHODS

    /**
     * Handles a NOSTR REQ-message by adding a new subscription to subs then sends EVENT feed for a new valid subscription back.
     * @param reqDataSet
     * parsed REQ-Message data SET
     */
    private void handleReq(Set<ReqData> reqDataSet) {
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
    private void handleClose(CloseData closeData) {
        if (subs.containsKey(closeData.subscription())) {
            subs.remove(closeData.subscription());
            System.out.println("subscription removed!");
        }
    }

    /**
     * Handles NOSTR EVENT-message by distributing EVENT-data to a proper logic-handling method defined by EVENT-kind
     * @param eventData
     * parsed EVENT-message data
     */
    private void handleEvent(EventData eventData) {
        switch (eventData.getKind()) {
            case 0      : handleEvent_0(eventData); break;
            case 1      : handleEvent_1(eventData); break;
            default     :
                try {
                    eventData.getSubscription().session().sendMessage(noticeMessage("Unknown event kind"));
                } catch (IOException e) {
                    System.out.println("Unknown event kind!");
                    throw new RuntimeException(e);
                }
        }
    }

    // SUBSCRIPTION

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
            session.sendMessage(eoseMessage(reqDataSet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Feeds current subscriptions with REQuested incoming EVENT-data
     * @param eventData
     * examined EVENT-data to filter
     */
    private void handleSubFeed(EventData eventData) {
        Set<EventData> validEventData = new HashSet<>();
        // filter incoming event by each ReqData in Map<Subscription, Set<ReqData>> - for each sub individually
        for (Map.Entry<Subscription, Set<ReqData>> entry : subs.entrySet()) {
            Set<ReqData> reqDataSet = entry.getValue();
            validEventData.addAll(filterSubFeed(eventData, reqDataSet));
        }
        sendSubFeed(validEventData);
    }

    /**
     * Compares EVENT-data to REQ-data specifics. Sets the right subscription data to event after filtering. Note there might be more REQ-data for single subscription (sub method)
     * @param eventData
     * EVENT-data to examine
     * @param reqDataSet
     * REQ-data SET to filter by
     * @return
     * compatible EVENT-data
     */
    private Set<EventData> filterSubFeed(EventData eventData, Set<ReqData> reqDataSet) {
        Set<EventData> validEventData = new HashSet<>();
        reqDataSet.forEach(r -> {
            // filter for kinds (event kinds)
            if ((r.getKinds() != null || r.getKinds().isEmpty()) && r.getKinds().contains(eventData.getKind())) {
                eventData.setSubscription(reqDataSet.iterator().next().getSubscription());
                validEventData.add(eventData);
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
                eventData.getSubscription().session().sendMessage(eventMessage(eventData));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // EVENTS

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
                eventData.getSubscription().session().sendMessage(noticeMessage("metadata updated"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // save new data, send OK message
        persistence.saveEvent(eventData);
        try {
            eventData.getSubscription().session().sendMessage(okMessage(eventData, true));
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
            eventData.getSubscription().session().sendMessage(okMessage(eventData, true));
        } catch (Exception e) {
            System.err.println("Session closed");
        }
    }

    // MESSAGES

    /**
     * Parses NOSTR OK-message to be sent to client
     * @param eventData
     * Mentioned EVENT-data
     * @param wasEventSaved
     * Indicates whether the EVENT was saved - set as needed
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    private TextMessage okMessage(EventData eventData, boolean wasEventSaved) {
        return new TextMessage("[\"OK\",\"" + eventData.getId() + "\"," + wasEventSaved + ",\"\"]");
    }

    /**
     * Parses NOSTR EVENT-message to be sent to client
     * @param eventData
     * Actual EventData to be sent back
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    private TextMessage eventMessage(EventData eventData) {
        try {
            return new TextMessage("[\"EVENT\",\"" + eventData.getSubscription().subscription_id() + "\","
                    + deser.getMapper().writeValueAsString(eventData) + "]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses NOSTR EOSE-message to be sent to client
     * @param reqDataSet
     * REQ-data SET related to subscription
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    private TextMessage eoseMessage(Set<ReqData> reqDataSet) {
        return new TextMessage("[\"EOSE\",\"" + reqDataSet.stream().findAny().get().getSubscription().subscription_id() + "\"]");
    }

    /**
     * Parses NOSTR NOTICE-message to be sent to cliend
     * @param message
     * info payload
     * @return
     * TextMessage to be sent to client
     */
    private TextMessage noticeMessage(String message) {
        return new TextMessage("[\"NOTICE\",\"" + message + "\"]");
    }
}
