package org.openskyt.nostrrelay.dto;

import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

public record Subscription(
        String subscription_id,
        WebSocketSession session,
        Set<ReqFilter> filters
){
}
