package com.eshop.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Entry point for the Catalog microservice.
 *
 * <p>Scans:
 * <ul>
 *   <li>{@code com.eshop.catalog} — all service components.</li>
 *   <li>{@code com.eshop.eventlog} — {@code IntegrationEventLogEntry} JPA entity and
 *       {@code IntegrationEventLogService} from the {@code common/event-log} module.</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = {"com.eshop.catalog", "com.eshop.eventlog", "com.eshop.eventbus"})
@EntityScan(basePackages = {"com.eshop.catalog.domain", "com.eshop.eventlog"})
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
