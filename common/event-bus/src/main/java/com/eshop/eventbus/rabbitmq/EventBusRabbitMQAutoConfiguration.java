package com.eshop.eventbus.rabbitmq;

import com.eshop.eventbus.EventBusSubscriptionsManager;
import com.eshop.eventbus.IEventBus;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot auto-configuration for the RabbitMQ event bus.
 *
 * <p>Activated automatically when {@link RabbitTemplate} is on the classpath
 * and no other {@link IEventBus} bean has been declared by the application.
 *
 * <p>Services must set {@code eshop.event-bus.queue-name} in their
 * {@code application.yml} to define the durable queue name for that service
 * (e.g. {@code Catalog}, {@code Ordering}, {@code Basket}).
 */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
@Import(RabbitMQConfig.class)
public class EventBusRabbitMQAutoConfiguration {

    @Value("${eshop.event-bus.queue-name:eshop-default}")
    private String queueName;

    @Bean
    @ConditionalOnMissingBean(RabbitAdmin.class)
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate.getConnectionFactory());
    }

    @Bean
    @ConditionalOnMissingBean(EventBusSubscriptionsManager.class)
    public EventBusSubscriptionsManager eventBusSubscriptionsManager() {
        return new EventBusSubscriptionsManager();
    }

    @Bean
    @ConditionalOnMissingBean(IEventBus.class)
    public IEventBus eventBus(
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin,
            EventBusSubscriptionsManager subscriptionsManager,
            ApplicationContext applicationContext) {
        return new RabbitMQEventBus(
                rabbitTemplate,
                rabbitAdmin,
                subscriptionsManager,
                applicationContext,
                queueName);
    }
}
