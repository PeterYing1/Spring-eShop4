package com.eshop.orderingbackground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Ordering Background Tasks service.
 *
 * <p>This service is the Java/Spring equivalent of the .NET
 * {@code Ordering.BackgroundTasks} service. It periodically queries the
 * ordering database for orders in {@code submitted} status whose grace period
 * has expired, and publishes {@code GracePeriodConfirmedIntegrationEvent} to
 * the RabbitMQ event bus so the ordering service can advance those orders to
 * {@code awaitingvalidation}.
 *
 * <p>{@link EnableScheduling} activates Spring's {@code @Scheduled} support,
 * which drives {@link com.eshop.orderingbackground.tasks.GracePeriodManagerTask}.
 */
@SpringBootApplication
@EnableScheduling
public class OrderingBackgroundTasksApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderingBackgroundTasksApplication.class, args);
    }
}
