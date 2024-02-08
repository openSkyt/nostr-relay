package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.openskyt.nostrrelay.dto.*;
import org.openskyt.nostrrelay.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to deserialize NOSTR-messages into Java DTOs
 */
@Getter
@Component
public class NostrDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final NostrUtil util = new NostrUtil();
    private final Logger logger = LoggerFactory.getLogger(NostrDeserializer.class);

    /**
     * Deserializes incoming EVENT-message into EVENT-data object, adds session info. (uses sub method deserializeEvent())
     * @param messageJSON
     * incoming EVENT-message
     * @return
     * deserialized EVENT-data object
     */
    public Event deserializeEventMessage(String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length == 2) {
                // calling sub method
                return deserializeEvent(mapper.writeValueAsString(messageData[1]));
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize EVENT message", e);
        }
        return null;
    }

    /**
     * Deserializes incoming REQ-message into SET of REQ-data objects, adds subscription info. (uses sub method deserializeReq()) Note: there might be more ReqData in a single REQ-message that is why we use Set
     * @param messageJSON
     * incoming REQ message
     * @return
     * deserialized REQ-data SET
     */
    public Set<ReqFilter> deserializeReqMessage(String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData.length > 2) {
                Set<ReqFilter> reqFilterSet = new HashSet<>();
                for (int i = 2; i < messageData.length; i++) {
                    // calling sub method
                    reqFilterSet.add(deserializeReq(
                            mapper.writeValueAsString(
                                    messageData[i]))
                    );
                }
                return reqFilterSet;
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize REQ message", e);
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
            Object[] message = mapper.readValue(messageJSON, Object[].class);
            return new CloseData(session, message[1].toString());
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize CLOSE message", e);
        }
        return null; //todo check upper methods
    }

    /**
     * Sub method
     * @param eventJSON
     * incoming extracted EVENT json
     * @return
     * EVENT-object
     */
    private Event deserializeEvent(String eventJSON) {
        Event event;
        try {
            event = mapper.readValue(eventJSON, Event.class);
            return event;
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize EVENT message", e);
            return null; //todo check upper methods
        }
    }

    /**
     * Sub method
     * @param reqJSON
     * incoming extracted REQ json
     * @return
     * REQ-data
     */
    private ReqFilter deserializeReq(String reqJSON) {
        try {
            return mapper.readValue(reqJSON, ReqFilter.class);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize REQ message", e);
            return null;  //todo check upper methods
        }
    }
}