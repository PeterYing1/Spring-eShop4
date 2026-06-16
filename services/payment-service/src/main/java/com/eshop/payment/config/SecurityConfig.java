package com.eshop.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Payment service.
 *
 * <p>The Payment service has no REST API — it is purely event-driven via RabbitMQ.
 * Spring Security is still configured here to:
 * <ul>
 *   <li>Allow health-check and actuator endpoints used by Docker / orchestrators.</li>
 *   <li>Deny all other HTTP requests as a safe default (the service should not
 *       accept arbitrary HTTP traffic).</li>
 * </ul>
 *
 * <p>OAuth2 resource server configuration is intentionally omitted because
 * the service does not expose protected API endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/hc",
                                "/liveness",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}
