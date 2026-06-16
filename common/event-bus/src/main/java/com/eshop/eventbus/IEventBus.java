package com.eshop.eventbus;

/**
 * Abstraction over the underlying message broker used to publish and
 * subscribe to integration events.
 */
public interface IEventBus {

    /**
     * Publishes an integration event to the event bus exchange.
     * The routing key is the simple class name of the event.
     *
     * @param event the event to publish
     */
    void publish(IntegrationEvent event);
}
