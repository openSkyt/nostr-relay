package org.openskyt.nostrrelay.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

/**
 * Web-socket handler, logs incoming messages and tracks active sessions, distributes message to NostrMessageHandler
 */
@Component
@RequiredArgsConstructor
public class NostrController extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final NostrMessageHandler messageHandler;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        System.out.println("New session opened. Current session size: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull org.springframework.web.socket.CloseStatus status) {

        sessions.remove(session);
        System.out.println("Session closed. Current session size: " + sessions.size());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        String payload = message.getPayload();
        System.out.println("Message received: " + payload);
        messageHandler.handleMessage(session, payload);
    }


}
