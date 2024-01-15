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
    private final Map<Subscription, ReqData> subs = new HashMap<>();
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        String payload = message.getPayload();
        log.warn("Incoming message: " + payload);
        handleMessage(session, payload);
    }

    /**
     * Handles incoming generic message by checking NOSTR compatible types and invoking proper handling logic if any.
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
                default         : System.err.println("Invalid NOSTR message received");
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles NOSTR REQ-message by adding a subscription and sends EVENT feed for a new valid subscription back.
     * @param reqDataSet
     * parsed REQ-Message data SET
     */
    private void handleReq(Set<ReqData> reqDataSet) {
        reqDataSet.forEach(r -> {
            if (subs.containsKey(r.getSubscription())) {
                subs.remove(r.getSubscription());
                System.out.println("Sub rewrite");
            }
            subs.put(r.getSubscription(), r);
            System.out.println("new subscription added!");

        });
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
     * Handles NOSTR EVENT-message by distributing EVENT-data to a proper logic-handling method by EVENT-kind
     * @param eventData
     * parsed EVENT-message data
     */
    private void handleEvent(EventData eventData) {
        switch (eventData.getKind()) {
            case 0      : handleEvent_0(eventData); break;
            case 1      : handleEvent_1(eventData); break;
            default     : System.err.println("Unsupported kind received");
        }
    }

    /**
     * Feeds newly created subscription with REQuested existing EVENT-data
     * @param reqDataSet
     * incoming REQ-data SET
     */
    private void handleNewSubFeed(Set<ReqData> reqDataSet) {
        Set<EventData> eventDataSet = new HashSet<>();
        // filter db-events
        for (EventData eventData : persistence.retrieveAllEvents()) {
            reqDataSet.forEach(r -> eventDataSet.addAll(filterSubFeed(eventData, r)));
        }
        // send feed and eose for each subscription
        sendSubFeed(eventDataSet);
        reqDataSet.forEach(r -> {
            try {
                r.getSubscription().session().sendMessage(eoseMessage(r));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * Feeds current subscriptions with REQuested incoming EVENT-data
     * @param eventData
     * examined EVENT-data to filter
     */
    private void handleSubFeed(EventData eventData) {
        Set<EventData> eventDataSet = new HashSet<>();
        for (Map.Entry<Subscription, ReqData> entry : subs.entrySet()) {
            ReqData reqData = entry.getValue();
            eventDataSet.addAll(filterSubFeed(eventData, reqData));
        }
        sendSubFeed(eventDataSet);
    }

    /**
     * Compares EVENT-data to REQ-data specifics
     * @param eventData
     * EVENT-data to examine
     * @param reqData
     * REQ-data to filter by
     * @return
     * compatible EVENT-data
     */
    private Set<EventData> filterSubFeed(EventData eventData, ReqData reqData) {
        Set<EventData> validEventData = new HashSet<>();
        if (!reqData.getAuthors().isEmpty()) {
            if (reqData.getAuthors().contains(eventData.getPubkey())) {
                eventData.setSubscription(reqData.getSubscription());
                validEventData.add(eventData);
            }
        }
        return validEventData;
    }

    /**
     * Sends EVENT-data SET to client
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

    /**
     * Handles EVENT-messages kind 0
     * @param eventData
     * incoming EVENT-data
     */
    private void handleEvent_0(EventData eventData) {
        System.out.println("Event kind 0 received!" + eventData);
    }

    /**
     * Handles EVENT-messages kind 1 by saving into the DB. Responds with OK-message.
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
            return new TextMessage("[\"EVENT\",\"" + eventData.getSubscription().subscription_id() + "\"," + deser.getMapper().writeValueAsString(eventData) + "]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses NOSTR EOSE-message to be sent to client
     * @param reqData
     * REQ-data related to subscription
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    private TextMessage eoseMessage(ReqData reqData) {
        return new TextMessage("[\"EOSE\",\"" + reqData.getSubscription().subscription_id() + "\"]");
    }
}
