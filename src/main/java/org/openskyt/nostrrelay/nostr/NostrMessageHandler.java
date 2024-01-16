package org.openskyt.nostrrelay.nostr;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

/**
 * Generic message processing class. Deserializes message into valid NOSTR-compatible data and distributes to NostrEventHandler and NostrSubscriptionHandler
 */
@Component
@RequiredArgsConstructor
public class NostrMessageHandler {

    private final NostrSubscriptionHandler subscriptionHandler;
    private final NostrEventHandler eventHandler;
    private final NostrDeserializer deserializer;
    private final NostrUtil util;

    /**
     * Handles incoming generic message by checking NOSTR compatible types and invoking proper handling logic.
     * @param session
     * current ws-session
     * @param messageJSON
     * incoming message payload
     */
    public void handleMessage(WebSocketSession session, String messageJSON) {
        try {
            Object[] message = deserializer.getMapper().readValue(messageJSON, Object[].class);
            switch (message[0].toString()) {
                case "REQ"      : Set<ReqData> reqDataSet = deserializer.deserializeReqMessage(session, messageJSON);
                    subscriptionHandler.handleReq(reqDataSet); // handling method
                    break;
                case "CLOSE"    : CloseData closeData = deserializer.deserializeCloseMessage(session, messageJSON);
                    subscriptionHandler.handleClose(closeData); // handling method
                    break;
                case "EVENT"    : EventData eventData = deserializer.deserializeEventMessage(session, messageJSON);
                    eventHandler.handleEvent(eventData); // handling method
                    subscriptionHandler.handleSubFeed(eventData); // after receiving event -> broadcast
                    break;
                default         : session.sendMessage(util.noticeMessage("invalid NOSTR message"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
