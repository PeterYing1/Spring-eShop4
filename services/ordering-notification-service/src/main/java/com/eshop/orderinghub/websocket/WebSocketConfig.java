package com.eshop.orderinghub.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.eshop.orderinghub.config.JwtChannelInterceptor;

/**
 * Configures the STOMP-over-WebSocket message broker.
 *
 * <p>Clients connect to {@code /hub/notificationhub} (with optional SockJS
 * fallback) and subscribe to:
 * <ul>
 *   <li>{@code /topic/orders/{orderId}} — broadcasts for a specific order</li>
 *   <li>{@code /user/queue/orders} — personal queue routed to the authenticated user</li>
 * </ul>
 *
 * <p>The application destination prefix {@code /app} is reserved for
 * client-to-server messages (none in this service, but declared for
 * future use and completeness).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory simple broker; handles /topic and /user destinations
        registry.enableSimpleBroker("/topic", "/user");
        // Prefix for messages routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // Enable user-specific destinations via SimpMessagingTemplate.convertAndSendToUser
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/hub/notificationhub")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Validate JWT on STOMP CONNECT frames and set the Spring Security principal
        registration.interceptors(jwtChannelInterceptor);
    }
}
