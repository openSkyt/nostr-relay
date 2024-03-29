package org.openskyt.nostrrelay.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openskyt.nostrrelay.observers.SessionObserver;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

/**
 * Web-socket handler, logs incoming messages and tracks active sessions, delegates messages to NostrMessageHandler.handleMessage();
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NostrWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Router router;
    private final SessionObserver observer;


    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {

        sessions.add(session);
        log.info("DEV: New session opened. Current session size: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      CloseStatus status) {

        observer.notifyConsumers(session);
        sessions.remove(session);
        log.info("DEV: WebSocket session closed with reason: " + status.getReason());
        log.info("DEV: Current session size: " + sessions.size());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        // System.out.print(".");
        router.handleWSPayload(session, message.getPayload());
    }
}