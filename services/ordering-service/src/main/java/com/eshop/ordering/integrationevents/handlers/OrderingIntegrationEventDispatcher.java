package com.eshop.ordering.integrationevents.handlers;

import com.eshop.ordering.integrationevents.events.GracePeriodConfirmedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderPaymentFailedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderPaymentSucceededIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStockConfirmedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStockRejectedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.UserCheckoutAcceptedIntegrationEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Single {@code @RabbitListener} that consumes all messages from the
 * {@code Ordering} queue and dispatches them to the appropriate typed handler.
 *
 * <p>Messages from .NET publishers do not carry a {@code __TypeId__} header,
 * so we inspect the RabbitMQ routing key (stored in message properties) to
 * determine the event type, then deserialise the JSON body accordingly.
 *
 * <p>The ObjectMapper uses PascalCase naming to match .NET serialization.
 */
@Component
public class OrderingIntegrationEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OrderingIntegrationEventDispatcher.class);

    private final ObjectMapper mapper;

    private final UserCheckoutAcceptedIntegrationEventHandler userCheckoutHandler;
    private final GracePeriodConfirmedIntegrationEventHandler gracePeriodHandler;
    private final OrderPaymentSucceededIntegrationEventHandler paymentSucceededHandler;
    private final OrderPaymentFailedIntegrationEventHandler paymentFailedHandler;
    private final OrderStockConfirmedIntegrationEventHandler stockConfirmedHandler;
    private final OrderStockRejectedIntegrationEventHandler stockRejectedHandler;

    public OrderingIntegrationEventDispatcher(
            UserCheckoutAcceptedIntegrationEventHandler userCheckoutHandler,
            GracePeriodConfirmedIntegrationEventHandler gracePeriodHandler,
            OrderPaymentSucceededIntegrationEventHandler paymentSucceededHandler,
            OrderPaymentFailedIntegrationEventHandler paymentFailedHandler,
            OrderStockConfirmedIntegrationEventHandler stockConfirmedHandler,
            OrderStockRejectedIntegrationEventHandler stockRejectedHandler) {
        this.userCheckoutHandler = userCheckoutHandler;
        this.gracePeriodHandler = gracePeriodHandler;
        this.paymentSucceededHandler = paymentSucceededHandler;
        this.paymentFailedHandler = paymentFailedHandler;
        this.stockConfirmedHandler = stockConfirmedHandler;
        this.stockRejectedHandler = stockRejectedHandler;

        this.mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @RabbitListener(queues = "Ordering", concurrency = "1-5")
    public void dispatch(Message message) throws Exception {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody());

        log.debug("Received message on Ordering queue, routingKey='{}', bodyLength={}",
                routingKey, body.length());

        if (routingKey == null) {
            // Fallback: try to infer from body
            log.warn("Message has no routing key — attempting body-based dispatch");
            routingKey = inferTypeFromBody(body);
        }

        switch (routingKey) {
            case "UserCheckoutAcceptedIntegrationEvent":
                userCheckoutHandler.handle(
                        mapper.readValue(body, UserCheckoutAcceptedIntegrationEvent.class));
                break;

            case "GracePeriodConfirmedIntegrationEvent":
                gracePeriodHandler.handle(
                        mapper.readValue(body, GracePeriodConfirmedIntegrationEvent.class));
                break;

            case "OrderPaymentSucceededIntegrationEvent":
                paymentSucceededHandler.handle(
                        mapper.readValue(body, OrderPaymentSucceededIntegrationEvent.class));
                break;

            case "OrderPaymentFailedIntegrationEvent":
                paymentFailedHandler.handle(
                        mapper.readValue(body, OrderPaymentFailedIntegrationEvent.class));
                break;

            case "OrderStockConfirmedIntegrationEvent":
                stockConfirmedHandler.handle(
                        mapper.readValue(body, OrderStockConfirmedIntegrationEvent.class));
                break;

            case "OrderStockRejectedIntegrationEvent":
                stockRejectedHandler.handle(
                        mapper.readValue(body, OrderStockRejectedIntegrationEvent.class));
                break;

            default:
                log.warn("No handler registered for event type '{}' — message discarded", routingKey);
        }
    }

    private String inferTypeFromBody(String body) {
        try {
            // The .NET publisher does not add a type discriminator field,
            // but we can make a best-effort guess from field presence.
            JsonNode node = mapper.readTree(body);
            if (node.has("Basket")) return "UserCheckoutAcceptedIntegrationEvent";
            if (node.has("OrderStockItems") && !node.has("OrderStatus")) return "OrderStockRejectedIntegrationEvent";
            if (node.has("OrderStockItems")) return "OrderStatusChangedToAwaitingValidationIntegrationEvent";
            if (node.has("UserId") && !node.has("Basket")) return "OrderStartedIntegrationEvent";
            if (node.has("BuyerName") && node.has("OrderDate")) return "OrderStatusChangedToPaidIntegrationEvent";
            if (node.has("BuyerName") && node.has("OrderStatus")) return "OrderStatusChangedToShippedIntegrationEvent";
            if (node.has("OrderId") && node.size() == 3) return "GracePeriodConfirmedIntegrationEvent";
        } catch (Exception e) {
            log.error("Failed to infer event type from body: {}", e.getMessage());
        }
        return "Unknown";
    }
}
