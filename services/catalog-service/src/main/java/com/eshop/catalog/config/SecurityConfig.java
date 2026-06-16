package com.eshop.catalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Catalog service.
 *
 * <p>The Catalog API is publicly readable per the .NET source (no {@code [Authorize]}
 * attribute on the controller), so all {@code /api/v1/catalog/**} routes are
 * permitted without authentication.  Write operations (PUT, POST, DELETE) are
 * still allowed unauthenticated to mirror the original behaviour — callers are
 * protected at the API gateway level by network policy.
 *
 * <p>Management endpoints ({@code /hc}, {@code /liveness}, {@code /actuator/**})
 * and Swagger UI are also permit-all.
 *
 * <p>OAuth2 resource server JWT validation is configured but not enforced on
 * catalog routes — it is available for future tightening.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:#{null}}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Catalog API is publicly accessible (no [Authorize] in .NET source)
                .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/catalog/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/catalog/**").permitAll()
                // Swagger UI and OpenAPI docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                 "/v3/api-docs/**", "/v3/api-docs").permitAll()
                // Actuator / health probes
                .requestMatchers("/hc", "/liveness", "/actuator/**").permitAll()
                // Root redirect
                .requestMatchers("/").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        // Configure JWT resource server only when an issuer URI is configured
        if (issuerUri != null && !issuerUri.isBlank()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        }

        return http.build();
    }
}
