package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openskyt.nostrrelay.dto.Subscription;
import org.openskyt.nostrrelay.model.Event;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

@Component
public class NostrUtil {

    /**
     * Parses NOSTR OK-message to be sent to client
     * @param event
     * Mentioned EVENT-data
     * @param wasEventSaved
     * Indicates whether the EVENT was saved - set as needed
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage okMessage(Event event, boolean wasEventSaved, String message) {
        return new TextMessage("[\"OK\",\"" + event.getId() + "\"," + wasEventSaved + ",\"" + message + "\"]");
    }

    /**
     * Parses NOSTR EVENT-message to be sent to client
     * @param event
     * Actual EventData to be sent back
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage stringifyEvent(Event event, Subscription subscription) {
        try {
            return new TextMessage("[\"EVENT\",\"" + subscription.subscription_id() + "\","
                    + new ObjectMapper().writeValueAsString(event) + "]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses NOSTR EOSE-message to be sent to client
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage eoseMessage(Subscription subscription) {
        return new TextMessage("[\"EOSE\",\"" + subscription.subscription_id() + "\"]");
    }

    /**
     * Parses NOSTR NOTICE-message to be sent to cliend
     * @param message
     * info payload
     * @return
     * TextMessage to be sent to client
     */
    public TextMessage noticeMessage(String message) {
        return new TextMessage("[\"NOTICE\",\"" + message + "\"]");
    }
}