package com.eshop.ordering.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Core JPA and transaction configuration for the ordering service.
 *
 * <p>Scans domain packages for JPA entities and infrastructure package for
 * Spring Data repositories.
 *
 * <p>The common library packages ({@code com.eshop.eventlog}, {@code com.eshop.security})
 * are covered by Spring Boot auto-configuration (they supply {@code @AutoConfiguration}
 * classes via {@code spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}).
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = {
        "com.eshop.ordering.domain",
        "com.eshop.eventlog"
})
@EnableJpaRepositories(basePackages = {
        "com.eshop.ordering.infrastructure",
        "com.eshop.eventlog"
})
public class OrderingConfig {
}
