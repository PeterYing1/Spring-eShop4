package com.eshop.orderinghub.integrationevents.handlers;

import com.eshop.orderinghub.dto.OrderStatusNotification;
import com.eshop.orderinghub.integrationevents.events.OrderStatusChangedToShippedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handles {@link OrderStatusChangedToShippedIntegrationEvent} messages
 * from the {@code Ordering.signalrhub} queue and pushes a WebSocket notification
 * to the affected buyer.
 */
@Slf4j
@Component
public class OrderStatusChangedToShippedIntegrationEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusChangedToShippedIntegrationEventHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "Ordering.signalrhub", id = "shippedListener")
    public void handle(OrderStatusChangedToShippedIntegrationEvent event) {
        log.info("----- Handling integration event: {} - ({})", event.getId(), event);

        OrderStatusNotification notification = new OrderStatusNotification(
                event.getOrderId(),
                event.getOrderStatus(),
                event.getBuyerName(),
                Instant.now()
        );

        messagingTemplate.convertAndSendToUser(
                event.getBuyerName(),
                "/queue/orders",
                notification
        );

        messagingTemplate.convertAndSend("/topic/orders/" + event.getOrderId(), notification);
    }
}
