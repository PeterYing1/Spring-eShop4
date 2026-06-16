package com.eshop.webhooks.integrationevents.handlers;

import com.eshop.webhooks.domain.WebhookData;
import com.eshop.webhooks.domain.WebhookType;
import com.eshop.webhooks.integrationevents.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.webhooks.services.WebhooksRetriever;
import com.eshop.webhooks.services.WebhooksSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Handles {@link OrderStatusChangedToPaidIntegrationEvent} messages from the Ordering service.
 *
 * <p>Finds all webhook subscriptions of type {@code OrderPaid} and dispatches
 * the event payload to each subscriber's destination URL via {@link WebhooksSender}.
 */
@Slf4j
@Component
public class OrderStatusChangedToPaidIntegrationEventHandler {

    private final WebhooksRetriever webhooksRetriever;
    private final WebhooksSender webhooksSender;
    private final ObjectMapper integrationEventObjectMapper;

    public OrderStatusChangedToPaidIntegrationEventHandler(
            WebhooksRetriever webhooksRetriever,
            WebhooksSender webhooksSender,
            @Qualifier("integrationEventObjectMapper") ObjectMapper integrationEventObjectMapper) {
        this.webhooksRetriever = webhooksRetriever;
        this.webhooksSender = webhooksSender;
        this.integrationEventObjectMapper = integrationEventObjectMapper;
    }

    @RabbitListener(queues = "Webhooks",
            id = "webhooksOrderPaidListener",
            containerFactory = "rabbitListenerContainerFactory")
    public void handle(OrderStatusChangedToPaidIntegrationEvent event) {
        log.info("Handling OrderStatusChangedToPaidIntegrationEvent (id={}, orderId={})",
                event.getId(), event.getOrderId());

        var subscriptions = webhooksRetriever.getSubscriptions(WebhookType.ORDER_PAID.getTypeName());

        if (subscriptions.isEmpty()) {
            log.debug("No webhook subscriptions found for OrderPaid — nothing to dispatch.");
            return;
        }

        String payload;
        try {
            payload = integrationEventObjectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise OrderStatusChangedToPaidIntegrationEvent (id={}): {}",
                    event.getId(), e.getMessage(), e);
            return;
        }

        for (var subscription : subscriptions) {
            log.debug("Dispatching OrderPaid webhook to {} for user {}",
                    subscription.getDestUrl(), subscription.getUserId());
            var data = new WebhookData(
                    WebhookType.ORDER_PAID.getTypeName(),
                    payload,
                    subscription.getDestUrl(),
                    subscription.getToken());
            webhooksSender.send(data);
        }

        log.info("Dispatched OrderPaid webhook to {} subscriber(s)", subscriptions.size());
    }
}
