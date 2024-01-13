package org.openskyt.nostrrelay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NostrProtocol {

    private final ObjectMapper mapper;
    private final EventRepo repo;

    public void handleMessage(String messageJSON) {
        MessageData messageData = deserializeMessage(messageJSON);
        if (messageData == null) return;
        switchMessageAction(messageData);
    }

    private void switchMessageAction(MessageData messageData) {
        switch (messageData.type()) {
            case "EVENT"    : handleEvent(messageData.eventData()); break;
            case "REQ"      : System.out.println("REQ message received"); break;
            case "CLOSE"    : System.out.println("CLOSE message received"); break;
            default         : System.out.println("Unknown message type received");
        }
    }

    private void handleEvent(EventData eventData) {
        switch (eventData.kind()) {
            case 0      : System.out.println("Kind 0 event received"); break;
            case 1      : saveEvent(eventData); break;
            default     : System.out.println("Unknown kind event received");
        }
    }

    private MessageData deserializeMessage(String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData == null || messageData.length != 2) {
                throw new Exception();
            }
            return new MessageData(
                    messageData[0].toString(),
                    deserializeEvent(mapper.writeValueAsString(messageData[1]))
            );
        } catch (Exception e) {
            System.err.println("invalid NOSTR message received: " + messageJSON);
            return null;
        }
    }

    private EventData deserializeEvent(String eventJSON) {
        EventData eventData;
        try {
            eventData = mapper.readValue(eventJSON, EventData.class);
        } catch (JsonProcessingException e) {
            System.err.println("invalid NOSTR event received: " + eventJSON);
            return null;
        }
        return eventData;
    }

    private void saveEvent(EventData eventData) {
        repo.save(new Event(eventData.id()));
        dbLog();
    }

    public void dbLog() {
        System.out.println("nostr db log --------------------------------------");
        System.out.println("db: currently saved events: " + getEventCount());
        if (repo.findById(getEventCount()).isPresent()) {
            System.out.println("db: last saved event's id: " + repo.findById(getEventCount()).get().getContent());
        }
    }

    public long getEventCount() {
        return repo.count();
    }
}
