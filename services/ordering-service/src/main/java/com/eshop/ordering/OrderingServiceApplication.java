package com.eshop.ordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Entry point for the Ordering microservice.
 *
 * <p>Implements the Order bounded context with full DDD aggregate support,
 * CQRS command/query separation, and outbox-based integration events.
 *
 * <p>{@code com.eshop.eventlog} is explicitly scanned because the event-log
 * common module does not supply a Spring Boot auto-configuration class —
 * only its {@code @Service} and {@code @Repository} bean are needed.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.eshop.ordering",
        "com.eshop.eventlog"
})
public class OrderingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderingServiceApplication.class, args);
    }
}
