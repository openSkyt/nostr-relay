package org.openskyt.nostrrelay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class NostrProtocol extends TextWebSocketHandler {

    private final EventRepo repo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ReqData> subs = new HashMap<>();
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
        System.out.println("Incoming message: " + payload);
        handleMessage(payload, session);
    }

    private void handleMessage(String messageJSON, WebSocketSession session) {
        Object[] message;
        try {
            message = mapper.readValue(messageJSON, Object[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        switch (message[0].toString()) {
            case "EVENT"    : handleEvent(deserializeEventMessage(messageJSON, session)); break;
            case "REQ"      : handleReq(deserializeReqMessage(messageJSON, session)); break;
            case "CLOSE"    : handleClose(deserializeCloseMessage(messageJSON, session)); break;
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
        subs.remove(reqMessageData.subscription_id());
        for (ReqData req : reqMessageData.reqData()) {
            subs.put(reqMessageData.subscription_id(), req);
        }
        System.out.println("new subscription added!");
        System.out.println("current subs size: " + subs.size());
    }

    private void handleClose(CloseMessageData closeMessageData) {
        subs.remove(closeMessageData.subscription_id());
        System.out.println("subscription removed!");
        System.out.println("current subs size: " + subs.size());
    }

    private void handleEvent_0(EventData eventData) {
        System.out.println("Event kind 0 received!" + eventData);
    }

    private void handleEvent_1(EventData eventData) {
        saveEvent(eventData);
        try {
            eventData.getSession().sendMessage(okMessage(eventData.getId(), true));
        } catch (IOException e) {
            System.err.println("Session closed");
        }
    }

    private EventMessageData deserializeEventMessage(String messageJSON, WebSocketSession session) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length == 2) {
                return new EventMessageData(
                        messageData[0].toString(),
                        deserializeEvent(mapper.writeValueAsString(messageData[1]), session));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return null;
    }

    private ReqMessageData deserializeReqMessage(String messageJSON, WebSocketSession session) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length > 2) {
                List<ReqData> reqDataList = new ArrayList<>();
                for (int i = 2; i < messageData.length; i++) {
                    reqDataList.add(deserializeReq(mapper.writeValueAsString(messageData[i]), session, messageData[1].toString()));
                }
                return new ReqMessageData(
                        messageData[0].toString(),
                        messageData[1].toString(),
                        reqDataList
                );
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private CloseMessageData deserializeCloseMessage(String messageJSON, WebSocketSession session) {
        try {
            Object[] array = mapper.readValue(messageJSON, Object[].class);
            return  new CloseMessageData(
                    array[0].toString(),
                    array[1].toString(),
                    session
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private EventData deserializeEvent(String eventJSON, WebSocketSession session) {
        try {
            EventData eventData = mapper.readValue(eventJSON, EventData.class);
            eventData.setSession(session);
            return eventData;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private ReqData deserializeReq(String reqJSON, WebSocketSession session, String subscriptionId) {
        try {
            ReqData reqData = mapper.readValue(reqJSON, ReqData.class);
            reqData.setSession(session);
            reqData.setSubscription_id(subscriptionId);
            return reqData;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> deserializeFilters(String reqJSON) {
        System.out.println(reqJSON);
        return null;
    }

    private void saveEvent(EventData eventData) {
        repo.save(new Event(eventData.getId()));
        dbLog();
    }

    private TextMessage okMessage(String payload, boolean wasEventSaved) {
        return new TextMessage("[\"OK\",\"" + payload + "\"" + wasEventSaved + "]");
    }

    public void dbLog() {
        System.out.println("nostr db log --------------------------------------");
        System.out.println("db: currently saved events: " + getEventCount());
        if (repo.findById(getEventCount()).isPresent()) {
            System.out.println("db: last saved event's id: " + repo.findById(getEventCount()).get().getContent());
        }
    }

    public long getEventCount() {
        return repo.count();
    }
}
