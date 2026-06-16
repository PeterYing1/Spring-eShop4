package com.eshop.eventbus.rabbitmq;

import com.eshop.eventbus.EventBusSubscriptionsManager;
import com.eshop.eventbus.IEventBus;
import com.eshop.eventbus.IIntegrationEventHandler;
import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;

/**
 * RabbitMQ-backed implementation of {@link IEventBus}.
 *
 * <p><strong>Publishing:</strong> Events are serialised to JSON using a PascalCase
 * {@link ObjectMapper} and routed via the {@code eshop_event_bus} direct exchange.
 * The routing key is the simple class name of the event (e.g.
 * {@code OrderStartedIntegrationEvent}).
 *
 * <p><strong>Subscribing:</strong> Call {@link #subscribe(Class, String)} to bind a
 * handler bean to an event type.  The bus will declare a durable queue, bind it to
 * the exchange with the event name as routing key, and set up a
 * {@link SimpleMessageListenerContainer} that dispatches incoming messages to the
 * registered handler beans retrieved from the Spring {@link ApplicationContext}.
 */
public class RabbitMQEventBus implements IEventBus {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventBus.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final EventBusSubscriptionsManager subscriptionsManager;
    private final ApplicationContext applicationContext;
    private final String queueName;

    /**
     * Jackson ObjectMapper configured with PascalCase (UPPER_CAMEL_CASE) naming
     * strategy to match the .NET Newtonsoft.Json convention used for integration
     * event payloads.
     */
    private final ObjectMapper eventObjectMapper;

    public RabbitMQEventBus(
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin,
            EventBusSubscriptionsManager subscriptionsManager,
            ApplicationContext applicationContext,
            String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.subscriptionsManager = subscriptionsManager;
        this.applicationContext = applicationContext;
        this.queueName = queueName;
        this.eventObjectMapper = buildEventObjectMapper();
    }

    private static ObjectMapper buildEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // -------------------------------------------------------------------------
    // IEventBus
    // -------------------------------------------------------------------------

    /**
     * Serialises {@code event} to JSON with PascalCase property names and
     * publishes it to the {@code eshop_event_bus} exchange using the event's
     * simple class name as the routing key.
     *
     * @param event the integration event to publish
     * @throws RuntimeException if JSON serialisation fails
     */
    @Override
    public void publish(IntegrationEvent event) {
        String routingKey = event.getClass().getSimpleName();
        try {
            String json = eventObjectMapper.writeValueAsString(event);
            log.debug("Publishing event '{}' with id {}", routingKey, event.getId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, json);
        } catch (IOException e) {
            log.error("Failed to serialise event '{}': {}", routingKey, e.getMessage(), e);
            throw new RuntimeException("Failed to publish integration event: " + routingKey, e);
        }
    }

    // -------------------------------------------------------------------------
    // Subscription management
    // -------------------------------------------------------------------------

    /**
     * Registers {@code handlerBeanName} as a handler for {@code eventType}.
     *
     * <p>This method:
     * <ol>
     *   <li>Registers the mapping in {@link EventBusSubscriptionsManager}.</li>
     *   <li>Declares a durable queue named {@link #queueName} (idempotent).</li>
     *   <li>Binds the queue to the exchange with the event name as routing key.</li>
     *   <li>Starts a {@link SimpleMessageListenerContainer} that dispatches
     *       received messages to the handler.</li>
     * </ol>
     *
     * @param eventType       the integration event class
     * @param handlerBeanName Spring bean name implementing {@link IIntegrationEventHandler}
     * @param <T>             integration event type
     */
    public <T extends IntegrationEvent> void subscribe(Class<T> eventType, String handlerBeanName) {
        String eventName = eventType.getSimpleName();
        log.info("Subscribing to event '{}' with handler '{}'", eventName, handlerBeanName);

        subscriptionsManager.addSubscription(eventType, handlerBeanName);

        // Declare the service queue (durable, non-exclusive, non-auto-delete)
        Queue queue = new Queue(queueName, true, false, false);
        rabbitAdmin.declareQueue(queue);

        // Bind to the exchange using the event name as routing key
        DirectExchange exchange = new DirectExchange(RabbitMQConfig.EXCHANGE_NAME, true, false);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(eventName);
        rabbitAdmin.declareBinding(binding);

        // Start a listener for this queue
        startListenerContainer(eventName, eventType);
    }

    /**
     * Convenience overload that accepts the event's simple name as a string.
     * The event class must have been registered via
     * {@link #subscribe(Class, String)} beforehand so the type can be resolved
     * for deserialisation.
     *
     * @param eventName       simple class name of the event
     * @param handlerBeanName Spring bean name of the handler
     */
    public void subscribe(String eventName, String handlerBeanName) {
        log.info("Subscribing to event '{}' with handler '{}'", eventName, handlerBeanName);
        subscriptionsManager.addSubscription(eventName, handlerBeanName);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private <T extends IntegrationEvent> void startListenerContainer(
            String eventName, Class<T> eventType) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                rabbitTemplate.getConnectionFactory());
        container.setQueueNames(queueName);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(
                (ChannelAwareMessageListener) (message, channel) -> {
                    String body = new String(message.getBody());
                    List<String> handlerBeanNames = subscriptionsManager.getHandlersForEvent(eventName);

                    if (handlerBeanNames.isEmpty()) {
                        log.warn("No handlers registered for event '{}' — message discarded", eventName);
                        if (channel != null) {
                            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                        }
                        return;
                    }

                    T event = eventObjectMapper.readValue(body, eventType);

                    for (String beanName : handlerBeanNames) {
                        @SuppressWarnings("unchecked")
                        IIntegrationEventHandler<T> handler =
                                (IIntegrationEventHandler<T>) applicationContext.getBean(beanName);
                        try {
                            handler.handle(event);
                        } catch (Exception ex) {
                            log.error("Error handling event '{}' in '{}': {}",
                                    eventName, beanName, ex.getMessage(), ex);
                            if (channel != null) {
                                channel.basicNack(
                                        message.getMessageProperties().getDeliveryTag(),
                                        false,
                                        true);
                            }
                            return;
                        }
                    }

                    if (channel != null) {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    }
                });
        container.start();
    }
}
