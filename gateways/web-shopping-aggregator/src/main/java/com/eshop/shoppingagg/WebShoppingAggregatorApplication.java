package com.eshop.shoppingagg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.eshop.shoppingagg.config.AggregatorConfig;

/**
 * Entry point for the Web Shopping Aggregator BFF service.
 *
 * <p>This service composes calls to the catalog, basket, and ordering microservices
 * into cohesive responses for the WebMVC and WebSPA front-ends.  It runs on port
 * 5121 and is protected by OAuth2 JWT resource-server security.
 */
@SpringBootApplication
@EnableConfigurationProperties(AggregatorConfig.class)
public class WebShoppingAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebShoppingAggregatorApplication.class, args);
    }
}
