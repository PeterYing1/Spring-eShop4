package com.eshop.orderinghub.config;

import com.eshop.eventbus.rabbitmq.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for the Ordering Notification (SignalR Hub) service.
 *
 * <p>Declares a single durable queue named {@code Ordering.signalrhub} and
 * binds it to the shared {@code eshop_event_bus} direct exchange for each
 * of the six order-status-change integration events published by the Ordering
 * service.
 *
 * <p>The {@link MessageConverter} uses a PascalCase {@link ObjectMapper} so
 * that {@code @RabbitListener} handlers receive correctly-deserialized events
 * whose JSON property names follow the .NET Newtonsoft.Json PascalCase
 * convention.
 */
@Configuration
public class RabbitMQSignalRHubConfig {

    /** Durable queue name consumed by this service. */
    public static final String SIGNALR_HUB_QUEUE = "Ordering.signalrhub";

    // -------------------------------------------------------------------------
    // Queue
    // -------------------------------------------------------------------------

    @Bean
    public Queue signalrHubQueue() {
        return new Queue(SIGNALR_HUB_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Bindings — one per consumed event type
    // -------------------------------------------------------------------------

    @Bean
    public Binding orderStatusChangedToAwaitingValidationBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToAwaitingValidationIntegrationEvent");
    }

    @Bean
    public Binding orderStatusChangedToCancelledBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToCancelledIntegrationEvent");
    }

    @Bean
    public Binding orderStatusChangedToPaidBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToPaidIntegrationEvent");
    }

    @Bean
    public Binding orderStatusChangedToShippedBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToShippedIntegrationEvent");
    }

    @Bean
    public Binding orderStatusChangedToStockConfirmedBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToStockConfirmedIntegrationEvent");
    }

    @Bean
    public Binding orderStatusChangedToSubmittedBinding(
            Queue signalrHubQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(signalrHubQueue).to(eshopEventBus)
                .with("OrderStatusChangedToSubmittedIntegrationEvent");
    }

    // -------------------------------------------------------------------------
    // Message converter
    // -------------------------------------------------------------------------

    /**
     * Configures the AMQP message converter to use a PascalCase ObjectMapper,
     * matching the JSON property naming convention used by the .NET publishers.
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
