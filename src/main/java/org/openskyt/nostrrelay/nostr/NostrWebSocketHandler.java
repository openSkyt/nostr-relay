package org.openskyt.nostrrelay.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.logging.Logger;

/**
 * Web-socket handler, logs incoming messages and tracks active sessions, delegates messages to NostrMessageHandler.handleMessage();
 */
@Component
@RequiredArgsConstructor
public class NostrWebSocketHandler extends TextWebSocketHandler {

    Logger logger = Logger.getLogger(NostrWebSocketHandler.class.getName());

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Router router;
    private final SessionObserver observer;


    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {

        sessions.add(session);

        logger.info("dev: New session opened. Current session size: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      CloseStatus status) {

        observer.notifyConsumers(session);
        sessions.remove(session);
        logger.info("dev: WebSocket session closed with reason: " + status.getReason());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        String payload = message.getPayload();
        System.out.println("Message received: " + payload);
        router.handleWSPayload(session, payload);
    }
}