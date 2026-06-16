package com.eshop.webspa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the WebSPA host.
 *
 * <p>This service is a pure static-file host. Authentication and authorization are handled
 * entirely by the Angular application itself via the Keycloak JavaScript adapter. The Spring
 * Boot host therefore permits all requests and disables CSRF (there are no server-side state-
 * changing operations to protect).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Permit every request — the Angular app manages its own auth flow.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // No server-side state-changing endpoints, so CSRF protection is unnecessary.
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
