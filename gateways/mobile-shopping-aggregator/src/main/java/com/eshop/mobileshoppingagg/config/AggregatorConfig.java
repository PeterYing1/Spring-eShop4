package com.eshop.mobileshoppingagg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the {@code urls} block from {@code application.yml} to provide
 * downstream service base URLs.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "urls")
public class AggregatorConfig {

    /** Base URL of the catalog-service (e.g. {@code http://catalog-api:5101}). */
    private String catalogUrl;

    /** Base URL of the basket-service (e.g. {@code http://basket-api:5103}). */
    private String basketUrl;

    /** Base URL of the ordering-service (e.g. {@code http://ordering-api:5102}). */
    private String orderingUrl;
}
