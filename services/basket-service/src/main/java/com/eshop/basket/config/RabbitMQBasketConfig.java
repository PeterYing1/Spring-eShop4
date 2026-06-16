package com.eshop.basket.config;

import com.eshop.basket.integrationevents.events.OrderStartedIntegrationEvent;
import com.eshop.basket.integrationevents.events.ProductPriceChangedIntegrationEvent;
import com.eshop.eventbus.IEventBus;
import com.eshop.eventbus.rabbitmq.RabbitMQEventBus;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology and event-bus subscription configuration for the Basket service.
 *
 * <p>Declares the durable {@code Basket} queue and binds it to the
 * {@code eshop_event_bus} direct exchange for each event this service consumes:
 * <ul>
 *   <li>{@link ProductPriceChangedIntegrationEvent}</li>
 *   <li>{@link OrderStartedIntegrationEvent}</li>
 * </ul>
 *
 * <p>An {@link ApplicationRunner} registers the handler beans with the
 * {@link RabbitMQEventBus} at startup so the event bus dispatches inbound
 * messages to the correct handler.
 */
@Configuration
public class RabbitMQBasketConfig {

    /** Name of the durable queue used by the Basket service. */
    public static final String BASKET_QUEUE = "Basket";

    @Bean
    public Queue basketQueue() {
        return new Queue(BASKET_QUEUE, true, false, false);
    }

    @Bean
    public Binding productPriceChangedBinding(Queue basketQueue, DirectExchange eshopEventBus) {
        return BindingBuilder
                .bind(basketQueue)
                .to(eshopEventBus)
                .with(ProductPriceChangedIntegrationEvent.class.getSimpleName());
    }

    @Bean
    public Binding orderStartedBinding(Queue basketQueue, DirectExchange eshopEventBus) {
        return BindingBuilder
                .bind(basketQueue)
                .to(eshopEventBus)
                .with(OrderStartedIntegrationEvent.class.getSimpleName());
    }

    /**
     * Registers event handler subscriptions with the event bus at application startup.
     * The {@link RabbitMQEventBus#subscribe(Class, String)} call:
     * <ol>
     *   <li>Records the event-type → handler-bean-name mapping in
     *       {@code EventBusSubscriptionsManager}.</li>
     *   <li>Declares the queue and binding (idempotent).</li>
     *   <li>Starts a {@code SimpleMessageListenerContainer} that dispatches
     *       inbound messages to the handler bean.</li>
     * </ol>
     *
     * <p>Spring's default bean name for a {@code @Component} is the simple class
     * name with the first letter lowercased.
     */
    @Bean
    public ApplicationRunner basketEventBusSubscriptions(IEventBus eventBus) {
        return args -> {
            if (eventBus instanceof RabbitMQEventBus rabbitMQEventBus) {
                // Spring default bean names: first letter lowercased
                rabbitMQEventBus.subscribe(
                        ProductPriceChangedIntegrationEvent.class,
                        "productPriceChangedIntegrationEventHandler");

                rabbitMQEventBus.subscribe(
                        OrderStartedIntegrationEvent.class,
                        "orderStartedIntegrationEventHandler");
            }
        };
    }
}
