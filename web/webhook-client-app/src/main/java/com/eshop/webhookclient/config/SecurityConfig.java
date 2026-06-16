package com.eshop.webhookclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Exclude /webhook/receive from CSRF protection because it receives external POST callbacks
        // from the webhooks-service, which cannot supply a CSRF token.
        RequestMatcher receiverMatcher = new AntPathRequestMatcher("/webhook/receive");
        RequestMatcher csrfProtectedMatcher = new NegatedRequestMatcher(receiverMatcher);

        http
            .csrf(csrf -> csrf
                .requireCsrfProtectionMatcher(csrfProtectedMatcher)
            )
            .authorizeHttpRequests(auth -> auth
                // Public static resources and health endpoints
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico",
                    "/actuator/**",
                    "/hc",
                    "/liveness",
                    "/error"
                ).permitAll()
                // Webhook receiver is open — called by external service with token auth
                .requestMatchers("/webhook/**").permitAll()
                // Everything else (/ and /webhooks/**) requires login
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/webhooks", true)
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/account/signout"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/webhooks")
            );

        return http.build();
    }
}
