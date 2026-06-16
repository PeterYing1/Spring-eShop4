package com.eshop.marketing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Marketing service.
 *
 * <p>All {@code /api/v1/campaigns/**} routes require authentication
 * (enforced via {@code @PreAuthorize("isAuthenticated()")} on the controller).
 * Swagger UI, OpenAPI docs, actuator health probes, and the root redirect
 * are publicly accessible.
 *
 * <p>Method-level security ({@code @PreAuthorize}) is enabled via
 * {@link EnableMethodSecurity}.
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
                // Root redirect
                .requestMatchers("/").permitAll()
                // All campaign API routes require authentication
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
