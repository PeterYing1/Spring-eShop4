package com.eshop.webhooks.integrationevents.handlers;

import com.eshop.webhooks.domain.WebhookData;
import com.eshop.webhooks.domain.WebhookType;
import com.eshop.webhooks.integrationevents.events.ProductPriceChangedIntegrationEvent;
import com.eshop.webhooks.services.WebhooksRetriever;
import com.eshop.webhooks.services.WebhooksSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Handles {@link ProductPriceChangedIntegrationEvent} messages from the Catalog service.
 *
 * <p>Finds all webhook subscriptions of type {@code ProductPriceChanged} and dispatches
 * the event payload to each subscriber's destination URL via {@link WebhooksSender}.
 */
@Slf4j
@Component
public class ProductPriceChangedIntegrationEventHandler {

    private final WebhooksRetriever webhooksRetriever;
    private final WebhooksSender webhooksSender;
    private final ObjectMapper integrationEventObjectMapper;

    public ProductPriceChangedIntegrationEventHandler(
            WebhooksRetriever webhooksRetriever,
            WebhooksSender webhooksSender,
            @Qualifier("integrationEventObjectMapper") ObjectMapper integrationEventObjectMapper) {
        this.webhooksRetriever = webhooksRetriever;
        this.webhooksSender = webhooksSender;
        this.integrationEventObjectMapper = integrationEventObjectMapper;
    }

    @RabbitListener(queues = "Webhooks",
            id = "webhooksProductPriceChangedListener",
            containerFactory = "rabbitListenerContainerFactory")
    public void handle(ProductPriceChangedIntegrationEvent event) {
        log.info("Handling ProductPriceChangedIntegrationEvent (id={}, productId={}, newPrice={})",
                event.getId(), event.getProductId(), event.getNewPrice());

        var subscriptions = webhooksRetriever.getSubscriptions(WebhookType.PRODUCT_PRICE_CHANGED.getTypeName());

        if (subscriptions.isEmpty()) {
            log.debug("No webhook subscriptions found for ProductPriceChanged — nothing to dispatch.");
            return;
        }

        String payload;
        try {
            payload = integrationEventObjectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise ProductPriceChangedIntegrationEvent (id={}): {}",
                    event.getId(), e.getMessage(), e);
            return;
        }

        for (var subscription : subscriptions) {
            log.debug("Dispatching ProductPriceChanged webhook to {} for user {}",
                    subscription.getDestUrl(), subscription.getUserId());
            var data = new WebhookData(
                    WebhookType.PRODUCT_PRICE_CHANGED.getTypeName(),
                    payload,
                    subscription.getDestUrl(),
                    subscription.getToken());
            webhooksSender.send(data);
        }

        log.info("Dispatched ProductPriceChanged webhook to {} subscriber(s)", subscriptions.size());
    }
}
