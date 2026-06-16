package com.eshop.webhooks.config;

import com.eshop.eventbus.rabbitmq.RabbitMQConfig;
import com.eshop.webhooks.integrationevents.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.webhooks.integrationevents.events.ProductPriceChangedIntegrationEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ topology declarations for the Webhooks service.
 *
 * <p>Declares the durable {@code Webhooks} queue and binds it to the
 * {@code eshop_event_bus} direct exchange for each integration event that the
 * Webhooks service consumes:
 * <ul>
 *   <li>{@link OrderStatusChangedToPaidIntegrationEvent}</li>
 *   <li>{@link ProductPriceChangedIntegrationEvent}</li>
 * </ul>
 *
 * <p>The {@code rabbitListenerContainerFactory} is configured with the PascalCase
 * Jackson message converter so that inbound RabbitMQ messages are deserialised
 * with the naming convention used by the .NET publisher.
 */
@Configuration
public class RabbitMQWebhooksConfig {

    /** The durable queue name used by the Webhooks service. */
    public static final String WEBHOOKS_QUEUE = "Webhooks";

    // -------------------------------------------------------------------------
    // Queue declaration
    // -------------------------------------------------------------------------

    @Bean
    public Queue webhooksQueue() {
        return new Queue(WEBHOOKS_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Exchange (re-declared here idempotently — the common module already owns it)
    // -------------------------------------------------------------------------

    @Bean
    public DirectExchange eshopEventBusExchange() {
        return new DirectExchange(RabbitMQConfig.EXCHANGE_NAME, true, false);
    }

    // -------------------------------------------------------------------------
    // Bindings: one per event type consumed by the Webhooks service
    // -------------------------------------------------------------------------

    @Bean
    public Binding bindingOrderPaid(Queue webhooksQueue, DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(webhooksQueue)
                .to(eshopEventBusExchange)
                .with(OrderStatusChangedToPaidIntegrationEvent.class.getSimpleName());
    }

    @Bean
    public Binding bindingProductPriceChanged(Queue webhooksQueue, DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(webhooksQueue)
                .to(eshopEventBusExchange)
                .with(ProductPriceChangedIntegrationEvent.class.getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Listener container factory
    // -------------------------------------------------------------------------

    /**
     * Configures the {@code @RabbitListener} container factory for the Webhooks service.
     *
     * <p>Uses the PascalCase {@link Jackson2JsonMessageConverter} (backed by
     * {@code integrationEventObjectMapper}) so that inbound messages serialised
     * by the .NET application are correctly deserialised into Java event classes
     * that use {@code @JsonProperty("PascalCaseName")} annotations.
     *
     * @param connectionFactory          Spring AMQP connection factory
     * @param integrationEventObjectMapper PascalCase ObjectMapper bean
     * @return configured {@link SimpleRabbitListenerContainerFactory}
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("integrationEventObjectMapper") ObjectMapper integrationEventObjectMapper) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setMessageConverter(new Jackson2JsonMessageConverter(integrationEventObjectMapper));
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }
}
