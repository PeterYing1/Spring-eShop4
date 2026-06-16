package com.eshop.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level configuration for the Payment service.
 *
 * <p>Provides two beans:
 * <ul>
 *   <li><strong>integrationEventObjectMapper</strong> — a PascalCase
 *       {@link ObjectMapper} used to serialise outbound integration events in the
 *       naming convention expected by .NET consumers (Newtonsoft.Json defaults).</li>
 *   <li><strong>rabbitTemplate</strong> — a {@link RabbitTemplate} wired with a
 *       {@link Jackson2JsonMessageConverter} that uses the PascalCase mapper so
 *       that all published events have PascalCase property names on the wire.</li>
 * </ul>
 *
 * <p>No primary {@link ObjectMapper} is declared here because the Payment service
 * has no REST API layer that requires camelCase serialisation.
 */
@Configuration
public class PaymentConfig {

    /**
     * PascalCase {@link ObjectMapper} for integration events (RabbitMQ messages).
     * Named {@code integrationEventObjectMapper} so it can be injected by qualifier.
     */
    @Bean("integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * {@link RabbitTemplate} configured with a PascalCase message converter so that
     * all outbound integration events are serialised with PascalCase property names
     * compatible with the .NET Newtonsoft.Json convention.
     *
     * @param connectionFactory            Spring AMQP connection factory (auto-configured)
     * @param integrationEventObjectMapper PascalCase ObjectMapper declared above
     * @return configured {@link RabbitTemplate}
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("integrationEventObjectMapper") ObjectMapper integrationEventObjectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter(integrationEventObjectMapper));
        return template;
    }
}
