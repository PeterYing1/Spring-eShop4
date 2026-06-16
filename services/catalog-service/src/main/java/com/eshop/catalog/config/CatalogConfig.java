package com.eshop.catalog.config;

import com.eshop.catalog.application.CatalogSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main configuration class for the Catalog service.
 *
 * <ul>
 *   <li>Enables JPA repositories in the {@code com.eshop.catalog} package tree.</li>
 *   <li>Binds {@link CatalogSettings} from the {@code catalog.*} property prefix.</li>
 *   <li>Provides a secondary {@link ObjectMapper} bean named
 *       {@code integrationEventObjectMapper} that uses PascalCase property names
 *       to match the .NET Newtonsoft.Json convention for integration event payloads.</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(basePackages = {"com.eshop.catalog.infrastructure", "com.eshop.eventlog"})
@EnableConfigurationProperties(CatalogSettings.class)
public class CatalogConfig {

    /**
     * Secondary ObjectMapper for serialising / deserialising integration events.
     *
     * <p>Uses {@link PropertyNamingStrategies#UPPER_CAMEL_CASE} (PascalCase) to
     * produce JSON that is compatible with the .NET source application's
     * Newtonsoft.Json default naming convention.
     *
     * @return a configured {@link ObjectMapper} instance
     */
    @Bean("integrationEventObjectMapper")
    public ObjectMapper integrationEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
