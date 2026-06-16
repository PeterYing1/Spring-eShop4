package com.eshop.eventbus;

/**
 * Functional interface for handling a specific type of integration event.
 *
 * @param <T> the concrete integration event type this handler processes
 */
@FunctionalInterface
public interface IIntegrationEventHandler<T extends IntegrationEvent> {

    /**
     * Handles the given integration event.
     *
     * @param event the event to handle
     * @throws Exception if handling fails (causes nack and requeue in RabbitMQ listener)
     */
    void handle(T event) throws Exception;
}
