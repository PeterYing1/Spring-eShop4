package com.eshop.orderinghub.integrationevents.handlers;

import com.eshop.orderinghub.dto.OrderStatusNotification;
import com.eshop.orderinghub.integrationevents.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handles {@link OrderStatusChangedToAwaitingValidationIntegrationEvent} messages
 * from the {@code Ordering.signalrhub} queue and pushes a WebSocket notification
 * to the affected buyer.
 */
@Slf4j
@Component
public class OrderStatusChangedToAwaitingValidationIntegrationEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusChangedToAwaitingValidationIntegrationEventHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "Ordering.signalrhub", id = "awaitingValidationListener")
    public void handle(OrderStatusChangedToAwaitingValidationIntegrationEvent event) {
        log.info("----- Handling integration event: {} - ({})", event.getId(), event);

        OrderStatusNotification notification = new OrderStatusNotification(
                event.getOrderId(),
                event.getOrderStatus(),
                event.getBuyerName(),
                Instant.now()
        );

        // Push to the buyer's personal queue (requires authenticated STOMP principal)
        messagingTemplate.convertAndSendToUser(
                event.getBuyerName(),
                "/queue/orders",
                notification
        );

        // Also broadcast to all subscribers of this specific order's topic
        messagingTemplate.convertAndSend("/topic/orders/" + event.getOrderId(), notification);
    }
}
