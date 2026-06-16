package com.eshop.webhooks.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Webhooks service.
 *
 * <p>All {@code /api/v1/webhooks/**} routes require authentication
 * (enforced via {@code @PreAuthorize("isAuthenticated()")} on the controller).
 * Method-level security is enabled via {@code @EnableMethodSecurity}.
 *
 * <p>Management endpoints, Swagger UI, and the root redirect are permit-all.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:#{null}}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Swagger UI and OpenAPI docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/v3/api-docs").permitAll()
                // Actuator / health probes
                .requestMatchers("/hc", "/liveness", "/actuator/**").permitAll()
                // Root redirect to Swagger
                .requestMatchers(HttpMethod.GET, "/").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        // Configure JWT resource server only when an issuer URI is configured
        if (issuerUri != null && !issuerUri.isBlank()) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> {}));
        }

        return http.build();
    }
}
