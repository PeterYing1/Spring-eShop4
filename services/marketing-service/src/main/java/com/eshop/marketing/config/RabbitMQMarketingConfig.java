package com.eshop.marketing.config;

import com.eshop.marketing.integrationevents.events.UserLocationUpdatedIntegrationEvent;
import com.eshop.eventbus.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology declarations for the Marketing service.
 *
 * <p>Declares the durable {@code Marketing} queue and binds it to the
 * {@code eshop_event_bus} direct exchange for each event type consumed by
 * this service.
 *
 * <p>Consumed events:
 * <ul>
 *   <li>{@link UserLocationUpdatedIntegrationEvent} — updates the MongoDB
 *       location read model for the user</li>
 * </ul>
 */
@Configuration
public class RabbitMQMarketingConfig {

    /** Durable queue name for the Marketing service. */
    public static final String MARKETING_QUEUE = "Marketing";

    // -------------------------------------------------------------------------
    // Queue
    // -------------------------------------------------------------------------

    @Bean
    public Queue marketingQueue() {
        return new Queue(MARKETING_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Exchange (re-declared here for convenience — the common module already
    // declares it, but re-declaring is idempotent in RabbitMQ)
    // -------------------------------------------------------------------------

    @Bean
    public DirectExchange eshopEventBusExchange() {
        return new DirectExchange(RabbitMQConfig.EXCHANGE_NAME, true, false);
    }

    // -------------------------------------------------------------------------
    // Binding: UserLocationUpdatedIntegrationEvent → Marketing queue
    // -------------------------------------------------------------------------

    @Bean
    public Binding bindingUserLocationUpdated(Queue marketingQueue,
                                              DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(marketingQueue)
                .to(eshopEventBusExchange)
                .with(UserLocationUpdatedIntegrationEvent.class.getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Listener container factory
    // -------------------------------------------------------------------------

    /**
     * Configures the listener container factory used by the
     * {@code @RabbitListener} annotation in the event handler bean.
     *
     * @param connectionFactory the Spring AMQP connection factory
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
