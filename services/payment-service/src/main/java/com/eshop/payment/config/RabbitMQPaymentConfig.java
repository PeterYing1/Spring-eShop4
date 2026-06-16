package com.eshop.payment.config;

import com.eshop.eventbus.rabbitmq.RabbitMQConfig;
import com.eshop.payment.integrationevents.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology and listener container factory configuration for the Payment service.
 *
 * <p>Declares the durable {@code Payment} queue and binds it to the
 * {@code eshop_event_bus} direct exchange using the routing key
 * {@code OrderStatusChangedToStockConfirmedIntegrationEvent}.
 *
 * <p>The {@link SimpleRabbitListenerContainerFactory} bean named
 * {@code rabbitListenerContainerFactory} is wired with a PascalCase
 * {@link Jackson2JsonMessageConverter} so that inbound RabbitMQ messages
 * (serialised with .NET's Newtonsoft.Json PascalCase convention) are correctly
 * deserialised into Java event objects.
 */
@Configuration
public class RabbitMQPaymentConfig {

    /** The durable queue name used by the Payment service. */
    public static final String PAYMENT_QUEUE = "Payment";

    // -------------------------------------------------------------------------
    // Queue declaration
    // -------------------------------------------------------------------------

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true, false, false);
    }

    // -------------------------------------------------------------------------
    // Exchange (re-declared for convenience — idempotent in RabbitMQ)
    // -------------------------------------------------------------------------

    @Bean
    public DirectExchange eshopEventBusExchange() {
        return new DirectExchange(RabbitMQConfig.EXCHANGE_NAME, true, false);
    }

    // -------------------------------------------------------------------------
    // Bindings: one per event type consumed by Payment
    // -------------------------------------------------------------------------

    /**
     * Binds the {@code Payment} queue to the exchange using the routing key
     * {@code OrderStatusChangedToStockConfirmedIntegrationEvent}.
     *
     * @param paymentQueue        the durable Payment queue declared above
     * @param eshopEventBusExchange the shared direct exchange
     * @return the binding
     */
    @Bean
    public Binding bindingStockConfirmed(Queue paymentQueue, DirectExchange eshopEventBusExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(eshopEventBusExchange)
                .with(OrderStatusChangedToStockConfirmedIntegrationEvent.class.getSimpleName());
    }

    // -------------------------------------------------------------------------
    // Listener container factory
    // -------------------------------------------------------------------------

    /**
     * Configures the {@link SimpleRabbitListenerContainerFactory} used by
     * {@code @RabbitListener} annotations in the Payment service handlers.
     *
     * <p>Uses the PascalCase {@link Jackson2JsonMessageConverter} so that
     * inbound messages from the .NET Ordering service are deserialised
     * correctly.
     *
     * @param connectionFactory            Spring AMQP connection factory
     * @param integrationEventObjectMapper PascalCase ObjectMapper (from {@link PaymentConfig})
     * @return configured listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("integrationEventObjectMapper") ObjectMapper integrationEventObjectMapper) {

        Jackson2JsonMessageConverter messageConverter =
                new Jackson2JsonMessageConverter(integrationEventObjectMapper);

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        return factory;
    }
}
