package com.eshop.shoppingagg.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration properties and WebClient bean factory for the aggregator.
 *
 * <p>Downstream service base URLs are bound from the {@code urls.*} section of
 * {@code application.yml}.  Three {@link WebClient} beans are created — one per
 * downstream service — each pre-configured with the appropriate base URL.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "urls")
public class AggregatorConfig {

    /** Base URL of the catalog service (e.g. {@code http://catalog-api:5101}). */
    private String catalogUrl;

    /** Base URL of the basket service (e.g. {@code http://basket-api:5103}). */
    private String basketUrl;

    /** Base URL of the ordering service (e.g. {@code http://ordering-api:5102}). */
    private String orderingUrl;

    // -------------------------------------------------------------------------
    // WebClient beans
    // -------------------------------------------------------------------------

    /**
     * WebClient for the catalog service.
     *
     * <p>Catalog endpoints are public — no auth header is added here; individual
     * service calls do not need to forward a token.
     */
    @Bean("catalogServiceClient")
    public WebClient catalogServiceClient(WebClient.Builder builder) {
        log.info("Configuring catalogServiceClient → {}", catalogUrl);
        return builder.baseUrl(catalogUrl).build();
    }

    /**
     * WebClient for the basket service.
     *
     * <p>Basket endpoints require authentication.  The Bearer token is injected
     * per-request by the service layer (not globally here) so that each caller
     * can propagate the right user's token.
     */
    @Bean("basketServiceClient")
    public WebClient basketServiceClient(WebClient.Builder builder) {
        log.info("Configuring basketServiceClient → {}", basketUrl);
        return builder.baseUrl(basketUrl).build();
    }

    /**
     * WebClient for the ordering service.
     *
     * <p>Same token-forwarding approach as the basket client.
     */
    @Bean("orderingServiceClient")
    public WebClient orderingServiceClient(WebClient.Builder builder) {
        log.info("Configuring orderingServiceClient → {}", orderingUrl);
        return builder.baseUrl(orderingUrl).build();
    }
}
