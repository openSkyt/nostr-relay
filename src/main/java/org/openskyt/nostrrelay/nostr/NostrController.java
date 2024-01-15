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
    private final NostrDeserializer deserializer = new NostrDeserializer();
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

    private void handleMessage(WebSocketSession session, String messageJSON) {
        Object[] message;
        try {
            message = deserializer.getMapper().readValue(messageJSON, Object[].class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        switch (message[0].toString()) {
            case "REQ"      : handleReq(deserializer.deserializeReqMessage(messageJSON, session));
                              break;
            case "CLOSE"    : handleClose(deserializer.deserializeCloseMessage(messageJSON, session));
                              break;
            case "EVENT"    : EventMessageData eventMessageData = deserializer.deserializeEventMessage(messageJSON, session);
                              handleEvent(eventMessageData);
                              handleSubFeed(eventMessageData);
                              break;
            default         : System.err.println("Invalid NOSTR message received");
        }
    }

    private void handleEvent(EventMessageData eventMessageData) {
        switch (eventMessageData.eventData().getKind()) {
            case 0      : handleEvent_0(eventMessageData.eventData()); break;
            case 1      : handleEvent_1(eventMessageData.eventData()); break;
            default     : System.err.println("Unsupported kind received");
        }
    }

    private void handleReq(ReqMessageData reqMessageData) {
        Subscription subscription = new Subscription(reqMessageData.subscription_id(), reqMessageData.reqData().get(0).getSession());
        if (subs.containsKey(subscription)) {
            System.out.println("rewriting current sub..");
            subs.remove(subscription);
        }
        for (ReqData req : reqMessageData.reqData()) {
            subs.put(subscription, req);
        }
        System.out.println("new subscription added!");
        System.out.println("current subs size: " + subs.size());
        handleNewSubFeed(reqMessageData.reqData());
    }

    private void handleNewSubFeed(List<ReqData> reqDataList) {
        WebSocketSession session = reqDataList.get(0).getSession();
        String subscription_id = reqDataList.get(0).getSubscription_id();
        Set<EventData> eventDataSet = new HashSet<>();
        for (EventData eventData : persistence.retrieveAllEvents()) {
            reqDataList.forEach(r -> eventDataSet.addAll(filterSubFeed(eventData, r)));
        }
        sendSubFeed(eventDataSet);
        try {
            session.sendMessage(eoseMessage(subscription_id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSubFeed(EventMessageData eventMessageData) {
        Set<EventData> eventDataSet = new HashSet<>();
        for (Map.Entry<Subscription, ReqData> entry : subs.entrySet()) {
            ReqData reqData = entry.getValue();
            eventDataSet.addAll(filterSubFeed(eventMessageData.eventData(), reqData));
        }
        sendSubFeed(eventDataSet);
    }

    private Set<EventData> filterSubFeed(EventData eventData, ReqData reqData) {
        Set<EventData> e = new HashSet<>();
        if (!reqData.getAuthors().isEmpty()) {
            if (reqData.getAuthors().contains(eventData.getPubkey())) {
                e.add(eventData);
                eventData.setSession(reqData.getSession());
                eventData.setSubscription_id(reqData.getSubscription_id());
            }
        }
        return e;
    }

    private void sendSubFeed(Set<EventData> eventDataSet) {
        eventDataSet.forEach(e -> {
            try {
                e.getSession().sendMessage(eventMessage(e.getSubscription_id(), e));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }


    private void handleClose(CloseMessageData closeMessageData) {
        Subscription subscription = new Subscription(closeMessageData.subscription_id(), closeMessageData.session());
        if (!subs.containsKey(subscription)) {
            System.out.println("No sub like that found..");
            System.out.println("current subs size: " + subs.size());
            return;
        }
        subs.remove(subscription);
        System.out.println("subscription removed!");
        System.out.println("current subs size: " + subs.size());
    }

    private void handleEvent_0(EventData eventData) {
        System.out.println("Event kind 0 received!" + eventData);
    }

    private void handleEvent_1(EventData eventData) {
        try {
            persistence.saveEvent(eventData);
            eventData.getSession().sendMessage(okMessage(eventData.getId(), true));
        } catch (Exception e) {
            System.out.println("Session closed");
        }
    }

    private TextMessage okMessage(String payload, boolean wasEventSaved) {
        return new TextMessage("[\"OK\",\"" + payload + "\"," + wasEventSaved + ",\"\"]");
    }

    private TextMessage eventMessage(String subscription_id, EventData eventData) {
        try {
            return new TextMessage("[\"EVENT\",\"" + subscription_id + "\"," + deserializer.getMapper().writeValueAsString(eventData) + "]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private TextMessage eoseMessage(String subscription_id) {
        return new TextMessage("[\"EOSE\",\"" + subscription_id + "\"]");
    }
}
