package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.openskyt.nostrrelay.BIP340_Schnorr.EventSigValidator;
import org.openskyt.nostrrelay.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to deserialize NOSTR-messages into Java DTO
 */
@Getter
@Component
public class NostrDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final EventSigValidator validator = new EventSigValidator();
    private final NostrUtil util = new NostrUtil();

    /**
     * Deserializes incoming EVENT-message into EVENT-data object, adds session info. (uses sub method deserializeEvent())
     * @param session
     * current ws session
     * @param messageJSON
     * incoming EVENT-message
     * @return
     * deserialized EVENT-data object
     */
    public EventData deserializeEventMessage(WebSocketSession session, String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length == 2) {
                // calling sub method
                return deserializeEvent(session, mapper.writeValueAsString(messageData[1]));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Deserializes incoming REQ-message into SET of REQ-data objects, adds subscription info. (uses sub method deserializeReq()) Note: there might be more ReqData in a single REQ-message that is why we use Set
     * @param session
     * current ws session
     * @param messageJSON
     * incoming REQ message
     * @return
     * deserialized REQ-data SET
     */
    public Set<ReqData> deserializeReqMessage(WebSocketSession session, String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length > 2) {
                Set<ReqData> reqDataSet = new HashSet<>();
                for (int i = 2; i < messageData.length; i++) {
                    // calling sub method
                    reqDataSet.add(deserializeReq(
                            mapper.writeValueAsString(
                                    messageData[i]),
                                    session,
                                    messageData[1].toString())
                    );
                }
                return reqDataSet;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Deserializes incoming CLOSE-message to CLOSE-data object (
     * @param session
     * current ws session
     * @param messageJSON
     * incoming CLOSE-message
     * @return
     * deserialized CLOSE-data
     */
    public CloseData deserializeCloseMessage(WebSocketSession session, String messageJSON) {
        try {
            Object[] array = mapper.readValue(messageJSON, Object[].class);
            return  new CloseData(
                        new Subscription(
                            array[1].toString(),
                            session
                        )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sub method
     * @param session
     * current ws session
     * @param eventJSON
     * incoming extracted EVENT json
     * @return
     * EVENT-data
     */
    private EventData deserializeEvent(WebSocketSession session, String eventJSON) {
        try {
            EventData eventData = mapper.readValue(eventJSON, EventData.class);
            eventData.setSubscription(new Subscription(null, session));
            if (validator.verifyEvent(eventData)) {
                return eventData;
            };
            try {
                eventData.getSubscription().session().sendMessage(util.noticeMessage("invalid crypto data.."));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sub method
     * @param reqJSON
     * incoming extracted REQ json
     * @param session
     * current ws session
     * @param subscriptionId
     * incoming REQ-message subscription id
     * @return
     * REQ-data
     */
    private ReqData deserializeReq(String reqJSON, WebSocketSession session, String subscriptionId) {
        try {
            ReqData reqData = mapper.readValue(reqJSON, ReqData.class);
            reqData.setSubscription(
                    new Subscription(
                            subscriptionId,
                            session
                    )
            );
            return reqData;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
