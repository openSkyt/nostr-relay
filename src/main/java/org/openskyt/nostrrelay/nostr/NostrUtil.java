package org.openskyt.nostrrelay.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openskyt.nostrrelay.dto.EventData;
import org.openskyt.nostrrelay.dto.ReqData;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.Set;

@Component
public class NostrUtil {

    /**
     * Parses NOSTR OK-message to be sent to client
     * @param eventData
     * Mentioned EVENT-data
     * @param wasEventSaved
     * Indicates whether the EVENT was saved - set as needed
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage okMessage(EventData eventData, boolean wasEventSaved) {
        return new TextMessage("[\"OK\",\"" + eventData.getId() + "\"," + wasEventSaved + ",\"\"]");
    }

    /**
     * Parses NOSTR EVENT-message to be sent to client
     * @param eventData
     * Actual EventData to be sent back
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage eventMessage(EventData eventData) {
        try {
            return new TextMessage("[\"EVENT\",\"" + eventData.getSubscription().subscription_id() + "\","
                    + new ObjectMapper().writeValueAsString(eventData) + "]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses NOSTR EOSE-message to be sent to client
     * @param reqDataSet
     * REQ-data SET related to subscription
     * @return
     * TextMessage to be sent by WebSocketSession
     */
    public TextMessage eoseMessage(Set<ReqData> reqDataSet) {
        return new TextMessage("[\"EOSE\",\"" + reqDataSet.stream().findAny().get().getSubscription().subscription_id() + "\"]");
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
