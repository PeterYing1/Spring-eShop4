package com.eshop.catalog.config;

import com.eshop.catalog.integrationevents.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.catalog.integrationevents.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.eventbus.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology declarations for the Catalog service.
 *
 * <p>Declares the durable {@code Catalog} queue and binds it to the
 * {@code eshop_event_bus} direct exchange for each event type that the
 * Catalog service consumes.
 *
 * <p>Consumed events:
 * <ul>
 *   <li>{@link OrderStatusChangedToAwaitingValidationIntegrationEvent}</li>
 *   <li>{@link OrderStatusChangedToPaidIntegrationEvent}</li>
 * </ul>
 */
@Configuration
public class RabbitMQCatalogConfig {

    /** The durable queue name used by the Catalog service. */
    public static final String CATALOG_QUEUE = "Catalog";

    // -------------------------------------------------------------------------
    // Queue declaration
    // -------------------------------------------------------------------------

    @Bean
    public Queue catalogQueue() {
        return new Queue(CATALOG_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Exchange (re-declared here for convenience — the common module already
    // declares it, but declaring it again is idempotent in RabbitMQ)
    // -------------------------------------------------------------------------

    @Bean
    public DirectExchange eshopEventBusExchange() {
        return new DirectExchange(RabbitMQConfig.EXCHANGE_NAME, true, false);
    }

    // -------------------------------------------------------------------------
    // Bindings: one per event type consumed by Catalog
    // -------------------------------------------------------------------------

    @Bean
    public Binding bindingAwaitingValidation(Queue catalogQueue, DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(catalogQueue)
                .to(eshopEventBusExchange)
                .with(OrderStatusChangedToAwaitingValidationIntegrationEvent.class.getSimpleName());
    }

    @Bean
    public Binding bindingPaid(Queue catalogQueue, DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(catalogQueue)
                .to(eshopEventBusExchange)
                .with(OrderStatusChangedToPaidIntegrationEvent.class.getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Listener container factory
    // -------------------------------------------------------------------------

    /**
     * Configures the listener container factory used by the {@code @RabbitListener}
     * annotations in the event handler beans.  Uses manual acknowledgement so that
     * message are only acked after the handler completes successfully.
     *
     * @param connectionFactory the Spring AMQP connection factory (auto-configured
     *                          from {@code spring.rabbitmq.*} properties)
     * @return configured {@link SimpleRabbitListenerContainerFactory}
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }
}
