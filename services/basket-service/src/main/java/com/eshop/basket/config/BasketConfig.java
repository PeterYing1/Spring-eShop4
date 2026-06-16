package com.eshop.basket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Application-level configuration for the Basket service.
 *
 * <p>Declares two {@link ObjectMapper} beans:
 * <ul>
 *   <li><strong>Primary (default)</strong> — camelCase, used by the MVC layer
 *       (REST request/response) and by {@code RedisBasketRepository} for basket
 *       JSON stored in Redis.</li>
 *   <li><strong>integrationEventObjectMapper</strong> — PascalCase
 *       ({@link PropertyNamingStrategies#UPPER_CAMEL_CASE}), used by the
 *       {@link RabbitTemplate} message converter so that integration events are
 *       serialised with the naming convention expected by .NET consumers.</li>
 * </ul>
 */
@Configuration
public class BasketConfig {

    /**
     * Primary (camelCase) ObjectMapper — used for Redis basket storage and
     * the default Spring MVC JSON serialisation.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * PascalCase ObjectMapper for integration events (RabbitMQ messages).
     * Named {@code integrationEventObjectMapper} so it can be injected by
     * qualifier where needed.
     */
    @Bean("integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * RabbitTemplate configured with the PascalCase message converter so that
     * all outbound integration events are serialised with PascalCase property
     * names compatible with the .NET Newtonsoft.Json convention.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            @org.springframework.beans.factory.annotation.Qualifier("integrationEventObjectMapper")
            ObjectMapper integrationEventObjectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter(integrationEventObjectMapper));
        return template;
    }
}
