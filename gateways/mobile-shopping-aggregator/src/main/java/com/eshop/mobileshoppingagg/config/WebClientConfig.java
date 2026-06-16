package com.eshop.mobileshoppingagg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides a shared {@link WebClient.Builder} bean.
 *
 * <p>Individual services inject the builder and call {@code build()} (or set a
 * base URL) so each service gets its own isolated client instance.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
