package com.eshop.orderinghub.integrationevents.handlers;

import com.eshop.orderinghub.dto.OrderStatusNotification;
import com.eshop.orderinghub.integrationevents.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handles {@link OrderStatusChangedToStockConfirmedIntegrationEvent} messages
 * from the {@code Ordering.signalrhub} queue and pushes a WebSocket notification
 * to the affected buyer.
 */
@Slf4j
@Component
public class OrderStatusChangedToStockConfirmedIntegrationEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderStatusChangedToStockConfirmedIntegrationEventHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "Ordering.signalrhub", id = "stockConfirmedListener")
    public void handle(OrderStatusChangedToStockConfirmedIntegrationEvent event) {
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
