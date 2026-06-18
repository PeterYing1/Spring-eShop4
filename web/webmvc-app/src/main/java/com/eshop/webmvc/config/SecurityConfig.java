package com.eshop.webmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF stored in XSRF-TOKEN browser cookie — validation is independent of which
        // JSESSIONID the browser sends, so no session-CSRF mismatch after OAuth2 login.
        CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();

        // After OAuth2 login, replay the saved request (e.g. /cart/additem) if present,
        // otherwise fall back to /catalog.
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/catalog");

        http
            .securityContext(ctx -> ctx.requireExplicitSave(false))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfHandler)
            )
            // Disable session fixation protection so the JSESSIONID cookie that Chrome
            // "established" via the same-origin redirect from /cart/additem → /oauth2/...
            // keeps the same ID after login.  Without this, ChangeSessionIdAuthenticationStrategy
            // rotates the ID in the OAuth2 callback response (a cross-origin context), which
            // Chrome treats as a temporary/one-shot cookie and refuses to send on subsequent
            // navigations.  URL-based session tracking is already disabled (tracking-modes:
            // cookie), which closes the classic session-fixation attack vector.
            .sessionManagement(session -> session.sessionFixation().none())
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
                .successHandler(successHandler)
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
