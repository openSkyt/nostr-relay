package org.openskyt.nostrrelay.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Web-socket handler, logs incoming messages and tracks active sessions, delegates messages to NostrMessageHandler.handleMessage();
 */
@Component
@RequiredArgsConstructor
public class NostrWebSocketHandler extends TextWebSocketHandler {

    //private final Logger logger = Logger.getLogger(NostrWebSocketHandler.class.getName());
    private final Logger logger = LoggerFactory.getLogger(NostrWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Router router;
    private final SessionObserver observer;


    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {

        sessions.add(session);

        logger.info("INFO dev: New session opened. Current session size: " + sessions.size());
        logger.debug("DEBUG dev: New session opened. Current session size: " + sessions.size());
        logger.warn("WARNING dev: New session opened. Current session size: " + sessions.size());
        logger.error("ERROR dev: New session opened. Current session size: " + sessions.size());
        logger.trace("TRACE dev: New session opened. Current session size: " + sessions.size());
        logger.isWarnEnabled();
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
        logger.info("Message received: " + payload);
        router.handleWSPayload(session, payload);
    }
}