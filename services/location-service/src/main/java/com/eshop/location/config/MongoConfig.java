package com.eshop.location.config;

import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration for the Location service.
 *
 * <p>All MongoDB connection settings ({@code spring.data.mongodb.*}) are
 * handled by Spring Boot's auto-configuration.  This class exists as a
 * placeholder for any future customisations (e.g. custom converters or
 * index creation).
 */
@Configuration
public class MongoConfig {
    // Spring Boot auto-configures MongoTemplate and MongoClient from
    // spring.data.mongodb.* properties in application.yml.
    // No additional configuration is required for the Location service.
}
