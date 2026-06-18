package com.eshop.webmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Use CookieCsrfTokenRepository so the CSRF token is stored in an XSRF-TOKEN cookie
        // rather than the HTTP session. This avoids the mismatch that occurs when Spring
        // Security's CsrfAuthenticationStrategy clears the session CSRF token on login and
        // the new token isn't yet in the session when the catalog page is rendered.
        // CsrfTokenRequestAttributeHandler (no XOR masking) ensures ${_csrf.token} and
        // th:action both embed the same raw cookie value.
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/catalog",
                    "/catalog/**",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/catalog", true)
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestRepository(new StateKeyedOAuth2AuthorizationRequestRepository())
                )
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/account/signout"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/catalog")
            );
        return http.build();
    }
}
