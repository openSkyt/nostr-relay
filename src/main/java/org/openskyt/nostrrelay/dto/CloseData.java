package org.openskyt.nostrrelay.dto;

import org.springframework.web.socket.WebSocketSession;

public record CloseData(
        WebSocketSession session,
        String subscription_id
) {
}