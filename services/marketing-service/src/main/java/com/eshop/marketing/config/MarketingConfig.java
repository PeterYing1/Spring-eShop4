package com.eshop.marketing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main configuration class for the Marketing service.
 *
 * <p>Explicitly scopes JPA repository scanning and entity scanning to the SQL
 * domain package ({@code com.eshop.marketing.domain.sql}) so that Spring Boot's
 * JPA auto-configuration does not attempt to scan MongoDB document classes as
 * JPA entities.
 *
 * <p>Also provides the secondary {@code integrationEventObjectMapper} bean that
 * uses PascalCase property naming to match the .NET Newtonsoft.Json convention
 * expected by RabbitMQ consumers.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.eshop.marketing.domain.sql")
@EntityScan(basePackages = "com.eshop.marketing.domain.sql")
@EnableTransactionManagement
public class MarketingConfig {

    /**
     * PascalCase ObjectMapper for integration events serialised over RabbitMQ.
     *
     * @return configured {@link ObjectMapper} with
     *         {@link PropertyNamingStrategies#UPPER_CAMEL_CASE}
     */
    @Bean("integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
