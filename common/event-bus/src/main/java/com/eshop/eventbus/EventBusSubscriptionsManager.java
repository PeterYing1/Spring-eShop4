package com.eshop.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry that maps integration event type names to the Spring bean
 * names of their handlers.  Services populate this during startup via
 * {@link #addSubscription(String, String)} before the RabbitMQ listener
 * container starts.
 */
@Component
public class EventBusSubscriptionsManager {

    private static final Logger log = LoggerFactory.getLogger(EventBusSubscriptionsManager.class);

    /**
     * Key   = simple class name of the integration event (the RabbitMQ routing key).
     * Value = list of Spring bean names of handlers registered for that event.
     */
    private final Map<String, List<String>> handlers = new ConcurrentHashMap<>();

    /**
     * Reverse map used to resolve the event class name from its simple name
     * when looking up handlers at dispatch time.
     */
    private final Map<String, Class<? extends IntegrationEvent>> eventTypes = new ConcurrentHashMap<>();

    /**
     * Registers a handler bean for the given event name.
     *
     * @param eventName       simple class name of the integration event (e.g. {@code OrderStartedIntegrationEvent})
     * @param handlerBeanName name of the Spring bean that implements {@link IIntegrationEventHandler}
     */
    public void addSubscription(String eventName, String handlerBeanName) {
        log.debug("Registering handler '{}' for event '{}'", handlerBeanName, eventName);
        handlers.computeIfAbsent(eventName, k -> new ArrayList<>()).add(handlerBeanName);
    }

    /**
     * Registers a handler bean together with the event's concrete class so
     * that the dispatcher can deserialise the message payload.
     *
     * @param eventType       the concrete integration event class
     * @param handlerBeanName name of the Spring bean that implements {@link IIntegrationEventHandler}
     * @param <T>             integration event type
     */
    public <T extends IntegrationEvent> void addSubscription(
            Class<T> eventType, String handlerBeanName) {
        String eventName = eventType.getSimpleName();
        eventTypes.put(eventName, eventType);
        addSubscription(eventName, handlerBeanName);
    }

    /**
     * Returns the list of handler bean names registered for the given event name,
     * or an empty list if no handlers are registered.
     *
     * @param eventName simple class name of the event
     * @return unmodifiable list of handler bean names
     */
    public List<String> getHandlersForEvent(String eventName) {
        return Collections.unmodifiableList(
                handlers.getOrDefault(eventName, Collections.emptyList()));
    }

    /**
     * Returns {@code true} if at least one handler is registered for the
     * given event name.
     *
     * @param eventName simple class name of the event
     * @return {@code true} when subscriptions exist
     */
    public boolean hasSubscriptionsForEvent(String eventName) {
        return handlers.containsKey(eventName) && !handlers.get(eventName).isEmpty();
    }

    /**
     * Looks up the concrete event class by its simple name.  Returns
     * {@link Optional#empty()} when the class was not registered via
     * {@link #addSubscription(Class, String)}.
     *
     * @param eventName simple class name of the event
     * @return the event class, or empty
     */
    public Optional<Class<? extends IntegrationEvent>> getEventTypeByName(String eventName) {
        return Optional.ofNullable(eventTypes.get(eventName));
    }

    /**
     * Returns all registered event names (routing keys).
     *
     * @return unmodifiable set of event names
     */
    public Set<String> getAllEventNames() {
        return Collections.unmodifiableSet(handlers.keySet());
    }

    /**
     * Removes all registered subscriptions and event type mappings.
     * Primarily useful in tests.
     */
    public void clear() {
        handlers.clear();
        eventTypes.clear();
    }
}
