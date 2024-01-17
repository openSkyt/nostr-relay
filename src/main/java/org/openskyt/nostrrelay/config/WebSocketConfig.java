package org.openskyt.nostrrelay.config;

import lombok.RequiredArgsConstructor;
import org.openskyt.nostrrelay.nostr.NostrMessageHandler;
import org.openskyt.nostrrelay.nostr.NostrWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final NostrMessageHandler nostrMessageHandler;

    @Bean
    public NostrWebSocketHandler nostrWebSocketHandler() {
        return new NostrWebSocketHandler(nostrMessageHandler);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(nostrWebSocketHandler(), "/").setAllowedOrigins("*");
    }
}
