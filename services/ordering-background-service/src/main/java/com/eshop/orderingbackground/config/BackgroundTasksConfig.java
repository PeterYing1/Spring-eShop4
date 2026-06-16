package com.eshop.orderingbackground.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the ordering background tasks service.
 *
 * <p>Provides:
 * <ul>
 *   <li>A PascalCase {@link ObjectMapper} bean for serialising integration
 *       event payloads to match the .NET Newtonsoft.Json convention.</li>
 *   <li>A {@link MessageConverter} that wires the PascalCase mapper into
 *       the AMQP {@link RabbitTemplate} used by the event bus.</li>
 * </ul>
 *
 * <p>No JPA entity scanning or Flyway configuration is needed: this service
 * accesses the database via {@link org.springframework.jdbc.core.JdbcTemplate}
 * only, and the schema is owned by the ordering service.
 */
@Configuration
public class BackgroundTasksConfig {

    /**
     * ObjectMapper configured with PascalCase (UPPER_CAMEL_CASE) naming to
     * match the .NET integration event serialisation convention on RabbitMQ.
     */
    @Bean(name = "integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Configures the AMQP message converter with the PascalCase ObjectMapper so
     * that messages published via {@link RabbitTemplate} use the same JSON property
     * naming as the .NET publishers.
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter(integrationEventObjectMapper());
    }
}
