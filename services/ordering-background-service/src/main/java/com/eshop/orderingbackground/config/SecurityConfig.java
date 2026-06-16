package com.eshop.orderingbackground.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP security configuration for the ordering background tasks service.
 *
 * <p>This service exposes no REST API — only Spring Boot Actuator health
 * endpoints ({@code /hc}, {@code /liveness}, {@code /actuator/**}) are
 * available and are permitted without authentication. No OAuth2 resource
 * server is configured since there are no protected routes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/hc",
                                "/liveness",
                                "/actuator/**").permitAll()
                        .anyRequest().permitAll());

        return http.build();
    }
}
