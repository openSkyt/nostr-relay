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

    public MessageData deserializeMessage(String messageJSON) {
        try {
            Object[] messageData = mapper.readValue(messageJSON, Object[].class);
            if (messageData == null || messageData.length < 2) {
                return null;
            }
            return new MessageData(
                    messageData[0].toString(),
                    deserializeEvent(mapper.writeValueAsString(messageData[1]))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public EventData deserializeEvent(String eventJSON) {
        EventData eventData;
        try {
            eventData = mapper.readValue(eventJSON, EventData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return eventData;
    }

    public void saveEvent(EventData eventData) {
        repo.save(new Event(eventData.content()));
        dbLog();
    }

    public void dbLog() {
        System.out.println("nostr db log --------------------------------------");
        System.out.println("db: currently saved events: " + getEventCount());
        if (repo.findById(getEventCount()).isPresent()) {
            System.out.println("db: last saved event: " + repo.findById(getEventCount()).get());
        }
    }

    public long getEventCount() {
        return repo.count();
    }
}
