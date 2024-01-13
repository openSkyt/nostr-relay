package org.openskyt.nostrrelay;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final NostrProtocol nostr;
    private static final Set<WebSocketSession> SESSIONS = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {

        SESSIONS.add(session);
        System.out.println("---------------------------------------------------");
        System.out.println("New session established: " + session.getId());
        System.out.println("current SESSIONS size: " + SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,
                                      @NonNull org.springframework.web.socket.CloseStatus status) {

        SESSIONS.remove(session);
        System.out.println("---------------------------------------------------");
        System.out.println("Session closed: " + session.getId());
        System.out.println("current SESSIONS size: " + SESSIONS.size());
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  TextMessage message) {

        String payload = message.getPayload();
        SESSIONS.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        nostr.saveEvent(nostr.deserializeMessage(payload).eventData());
        System.out.println("deserialized object: " + nostr.deserializeMessage(payload).toString());
    }
}
