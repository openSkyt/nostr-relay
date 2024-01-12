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

    private static final Set<WebSocketSession> SESSIONS = Collections.synchronizedSet(new HashSet<>());
    private final EventRepo eventRepo;

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

        String payload = message.getPayload();              // get text-message content
        eventRepo.save(new Event(payload));                 // save to db
        SESSIONS.forEach(s -> {                             // broadcast to all active sessions
            try {
                s.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log(payload);                                       // custom logging
    }

    private void log(String payload) {
        System.out.println("---------------------------------------------------");
        System.out.println("Received Message: " + payload);
        System.out.println("current SESSIONS size: " + SESSIONS.size());
        System.out.println("db: events currently saved: " + eventRepo.count());
        if (eventRepo.findById(eventRepo.count()).isPresent()) {
            System.out.println("db: LAST EVENT FAKE-ID: " + eventRepo.findById(eventRepo.count()).get().getFakeId());
            System.out.println("db: LAST EVENT CONTENT:" + eventRepo.findById(eventRepo.count()).get().getContent());
        } else {
            System.err.println("db: no events found :(");
        }
    }
}
