package org.openskyt.nostrrelay;

import org.springframework.web.socket.WebSocketSession;

public record CloseMessageData(
        String type,
        String subscription_id,
        WebSocketSession session
) {
}
