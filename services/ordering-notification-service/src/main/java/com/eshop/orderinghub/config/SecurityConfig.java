package com.eshop.orderinghub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HTTP security configuration for the Ordering Notification Service.
 *
 * <p>WebSocket upgrade requests ({@code /hub/notificationhub/**}) are permitted
 * without HTTP-level authentication because the STOMP layer performs its own
 * JWT validation via {@link JwtChannelInterceptor}. All other HTTP endpoints
 * require a valid JWT bearer token.
 *
 * <p>Note: STOMP over WebSocket does not use the HTTP session, so
 * {@link SessionCreationPolicy#STATELESS} is used for the HTTP filter chain.
 * The principal is set per STOMP connection in {@link JwtChannelInterceptor}.
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
                        // WebSocket handshake endpoints — auth is handled at the STOMP level
                        .requestMatchers(
                                "/hub/notificationhub",
                                "/hub/notificationhub/**").permitAll()
                        // Actuator health / liveness
                        .requestMatchers(
                                "/hc",
                                "/liveness",
                                "/actuator/**").permitAll()
                        // Everything else requires a valid JWT
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {}));

        return http.build();
    }
}
