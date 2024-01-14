package org.openskyt.nostrrelay.dto;

import org.springframework.web.socket.WebSocketSession;

public record CloseMessageData(
        String type,
        String subscription_id,
        WebSocketSession session
) {
}
