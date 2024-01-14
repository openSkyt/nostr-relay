package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.openskyt.nostrrelay.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class NostrDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public EventMessageData deserializeEventMessage(String messageJSON, WebSocketSession session) {
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

    public ReqMessageData deserializeReqMessage(String messageJSON, WebSocketSession session) {
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

    public CloseMessageData deserializeCloseMessage(String messageJSON, WebSocketSession session) {
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

    public EventData deserializeEvent(String eventJSON, WebSocketSession session) {
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
}
