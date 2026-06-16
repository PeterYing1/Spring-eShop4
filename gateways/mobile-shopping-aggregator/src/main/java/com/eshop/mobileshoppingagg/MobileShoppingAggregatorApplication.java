package com.eshop.mobileshoppingagg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the Mobile Shopping BFF aggregator service.
 *
 * <p>This service runs on port {@code 5122} and acts as a Backend-for-Frontend
 * for the mobile shopping app.  It aggregates calls to:
 * <ul>
 *   <li>catalog-service — product listings and details</li>
 *   <li>basket-service  — basket read/write (with append-only item add)</li>
 *   <li>ordering-service — order draft creation</li>
 * </ul>
 */
@SpringBootApplication
@EnableConfigurationProperties
public class MobileShoppingAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MobileShoppingAggregatorApplication.class, args);
    }
}
