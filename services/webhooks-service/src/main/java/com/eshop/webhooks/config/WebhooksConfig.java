package com.eshop.webhooks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

/**
 * Main application configuration for the Webhooks service.
 *
 * <ul>
 *   <li>Enables JPA repositories in the {@code com.eshop.webhooks.infrastructure} package.</li>
 *   <li>Provides a primary camelCase {@link ObjectMapper} for REST responses.</li>
 *   <li>Provides a PascalCase {@code integrationEventObjectMapper} bean for
 *       serialising/deserialising integration event payloads to/from RabbitMQ.</li>
 *   <li>Configures {@link RabbitTemplate} with the PascalCase message converter.</li>
 *   <li>Provides a {@link RestTemplate} bean used by {@link com.eshop.webhooks.services.WebhooksSender}.</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.eshop.webhooks.infrastructure")
@EnableConfigurationProperties
public class WebhooksConfig {

    /**
     * Primary (camelCase) ObjectMapper used for REST API request/response serialisation.
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
     *
     * <p>Named {@code integrationEventObjectMapper} so it can be injected by qualifier
     * where needed (e.g., in event handlers for serialising outgoing webhook payloads).
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
     * outbound integration events are serialised with PascalCase property names
     * compatible with the .NET Newtonsoft.Json convention.
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

    /**
     * {@link RestTemplate} used by {@link com.eshop.webhooks.services.WebhooksSender}
     * to POST webhook payloads to subscriber destination URLs.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
