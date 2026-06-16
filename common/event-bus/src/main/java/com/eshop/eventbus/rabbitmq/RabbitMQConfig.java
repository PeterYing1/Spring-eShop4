package com.eshop.eventbus.rabbitmq;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the shared RabbitMQ exchange used by all eShop services.
 *
 * <p>Exchange characteristics:
 * <ul>
 *   <li>Name: {@code eshop_event_bus}</li>
 *   <li>Type: {@code direct}</li>
 *   <li>Durable: {@code true} — survives broker restarts</li>
 *   <li>Auto-delete: {@code false}</li>
 * </ul>
 *
 * <p>Each service binds its own durable queue to this exchange using the
 * simple class name of each event it wishes to consume as the routing key.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "eshop_event_bus";

    @Bean
    public DirectExchange eshopEventBus() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }
}
