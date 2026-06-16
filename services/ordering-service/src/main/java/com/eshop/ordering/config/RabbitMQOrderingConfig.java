package com.eshop.ordering.config;

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
 * RabbitMQ topology for the Ordering service.
 *
 * <p>Declares a single durable queue named {@code Ordering} and binds it to
 * the shared {@code eshop_event_bus} direct exchange for each integration event
 * type that the ordering service consumes.
 *
 * <p>The {@link MessageConverter} uses a PascalCase ObjectMapper so that
 * {@code @RabbitListener} handlers receive correctly deserialized events whose
 * JSON property names follow the .NET Newtonsoft.Json PascalCase convention.
 */
@Configuration
public class RabbitMQOrderingConfig {

    /** The durable queue name for the ordering service. */
    public static final String ORDERING_QUEUE = "Ordering";

    // -------------------------------------------------------------------------
    // Queue
    // -------------------------------------------------------------------------

    @Bean
    public Queue orderingQueue() {
        return new Queue(ORDERING_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Bindings — one per consumed event type
    // -------------------------------------------------------------------------

    @Bean
    public Binding userCheckoutAcceptedBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("UserCheckoutAcceptedIntegrationEvent");
    }

    @Bean
    public Binding gracePeriodConfirmedBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("GracePeriodConfirmedIntegrationEvent");
    }

    @Bean
    public Binding orderPaymentSucceededBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("OrderPaymentSucceededIntegrationEvent");
    }

    @Bean
    public Binding orderPaymentFailedBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("OrderPaymentFailedIntegrationEvent");
    }

    @Bean
    public Binding orderStockConfirmedBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("OrderStockConfirmedIntegrationEvent");
    }

    @Bean
    public Binding orderStockRejectedBinding(Queue orderingQueue, DirectExchange eshopEventBus) {
        return BindingBuilder.bind(orderingQueue).to(eshopEventBus)
                .with("OrderStockRejectedIntegrationEvent");
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
