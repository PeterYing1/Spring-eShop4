package com.eshop.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Location microservice.
 *
 * <p>Provides MongoDB-backed geospatial user location tracking and publishes
 * {@code UserLocationUpdatedIntegrationEvent} to the RabbitMQ event bus whenever
 * a user's current location changes.
 */
@SpringBootApplication
public class LocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
