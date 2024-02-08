package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.BIP340_Schnorr.EventSigValidator;
import org.openskyt.nostrrelay.dto.CloseData;
import org.openskyt.nostrrelay.dto.ReqFilter;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.observers.CloseObserver;
import org.openskyt.nostrrelay.observers.EventObserver;
import org.openskyt.nostrrelay.observers.ReqObserver;
import org.openskyt.nostrrelay.util.NostrDeserializer;
import org.openskyt.nostrrelay.util.NostrUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generic message processing class. Deserializes messages into valid NOSTR-compatible data with NostrDeserializer and delegates handling logic to appropriate observers
 */
@Component
@RequiredArgsConstructor
@EnableAsync
public class Router {

    private final NostrDeserializer deserializer;
    private final EventSigValidator sigValidator;
    private final NostrUtil util;

    private final CloseObserver closeObserver;
    private final ReqObserver reqObserver;
    private final EventObserver eventObserver;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void handleWSPayload(WebSocketSession session, String messageJSON) {
        CompletableFuture.runAsync(() -> {
            try {
                Object[] message = new ObjectMapper().readValue(messageJSON, Object[].class);
                switch (message[0].toString()) {
                    case "REQ":
                        Set<ReqFilter> reqFilter = deserializer.deserializeReqMessage(messageJSON);
                        Subscription sub = new Subscription(message[1].toString(), session, reqFilter);
                        reqObserver.notifyConsumers(sub);
                        break;
                    case "CLOSE":
                        CloseData closeData = deserializer.deserializeCloseMessage(session, messageJSON);
                        closeObserver.notifyConsumers(closeData);
                        break;
                    case "EVENT":
                        Event event = deserializer.deserializeEventMessage(messageJSON);
                        if (!sigValidator.verifyEvent(event)) {
                            sendMessageSafely(session, util.okMessage(event, false, "invalid crypto data"));
                            return;
                        }
                        sendMessageSafely(session, util.okMessage(event, true, ""));
                        //eventObserver.notifyConsumers(event);
                        break;
                    default:
                        sendMessageSafely(session, util.noticeMessage("Invalid NOSTR message"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    private synchronized void sendMessageSafely(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}