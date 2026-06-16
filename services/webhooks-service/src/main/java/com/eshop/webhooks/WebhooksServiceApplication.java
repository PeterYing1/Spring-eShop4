package com.eshop.webhooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Entry point for the Webhooks microservice.
 *
 * <p>Manages webhook subscriptions (CRUD REST API) and dispatches outbound
 * HTTP webhook payloads to registered subscriber URLs when integration events
 * arrive from RabbitMQ.
 *
 * <p>Scans:
 * <ul>
 *   <li>{@code com.eshop.webhooks} — all service components.</li>
 *   <li>{@code com.eshop.eventbus} — RabbitMQ event bus auto-configuration.</li>
 *   <li>{@code com.eshop.security} — shared {@code IIdentityService} / {@code IdentityService}.</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = {
        "com.eshop.webhooks",
        "com.eshop.eventbus",
        "com.eshop.security"
})
@EntityScan(basePackages = "com.eshop.webhooks.domain")
public class WebhooksServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebhooksServiceApplication.class, args);
    }
}
