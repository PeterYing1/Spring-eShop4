package com.eshop.orderinghub.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * STOMP controller that represents the notification hub endpoint.
 *
 * <p>This is the Spring equivalent of the .NET {@code NotificationsHub} class
 * that extends {@code Hub}. In the STOMP model there are no explicit
 * connect/disconnect overrides — the WebSocket lifecycle is managed by Spring's
 * message broker infrastructure. The JWT channel interceptor ({@link
 * com.eshop.orderinghub.config.JwtChannelInterceptor}) sets the authenticated
 * principal on every STOMP {@code CONNECT} frame.
 *
 * <p>Outbound notifications are pushed by the integration-event handlers using
 * {@link org.springframework.messaging.simp.SimpMessagingTemplate}; no inbound
 * {@code @MessageMapping} methods are needed for the current feature set.
 */
@Slf4j
@Controller
public class NotificationsHub {

    // No @MessageMapping methods required — the hub is purely a push endpoint.
    // Event handlers use SimpMessagingTemplate to broadcast order status changes.
}
