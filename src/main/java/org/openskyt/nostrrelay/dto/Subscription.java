package org.openskyt.nostrrelay.dto;

import org.springframework.web.socket.WebSocketSession;

public record Subscription(
        String subscription_id,
        WebSocketSession session
){
}
