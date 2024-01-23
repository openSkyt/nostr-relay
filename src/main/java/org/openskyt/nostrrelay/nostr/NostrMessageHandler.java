package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

/**
 * Generic message processing class. Deserializes messages into valid NOSTR-compatible data with NostrDeserializer and delegates handling logic to appropriate observers
 */
@Component
@RequiredArgsConstructor
public class NostrMessageHandler {

    private final NostrDeserializer deserializer;
    private final NostrUtil util;
    private final CloseObserver closeObserver;
    private final ReqObserver reqObserver;
    private final EventObserver eventObserver;

    /**
     * Handles incoming generic message by checking NOSTR compatible types and invoking appropriate handling logic.
     * @param session
     * current ws-session
     * @param messageJSON
     * incoming message payload
     */
    public void router(WebSocketSession session, String messageJSON) {
        try {
            Object[] message = new ObjectMapper().readValue(messageJSON, Object[].class);
            switch (message[0].toString()) {
                case "REQ"      :

                    Set<ReqData> reqDataSet = deserializer.deserializeReqMessage(messageJSON);
                    reqObserver.notifyConsumers(reqDataSet);
                    break;
                case "CLOSE"    :
                    CloseData closeData = deserializer.deserializeCloseMessage(session, messageJSON);
                    closeObserver.notifyConsumers(closeData);
                    break;
                case "EVENT"    :
                    Event event = deserializer.deserializeEventMessage(session, messageJSON);
                    try {
                        session.sendMessage(util.okMessage(event, true, ""));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // validate here
                    eventObserver.notifyConsumers(event);
                    break;
                default         :
                    session.sendMessage(util.noticeMessage("Invalid NOSTR message"));
                    System.out.println("MessageHandler: Invalid nostr message!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
