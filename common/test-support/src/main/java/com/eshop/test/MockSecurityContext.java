package com.eshop.test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Utility for setting up a mock Spring Security context in unit and slice tests.
 *
 * <p>Call one of the {@code withUser} variants in a {@code @BeforeEach} method
 * (or inline in a test), and call {@link #clear()} in {@code @AfterEach} to
 * prevent context leakage between tests.
 *
 * <pre>{@code
 * @BeforeEach
 * void setUp() {
 *     MockSecurityContext.withUser("user-123", "alice");
 * }
 *
 * @AfterEach
 * void tearDown() {
 *     MockSecurityContext.clear();
 * }
 * }</pre>
 */
public final class MockSecurityContext {

    private MockSecurityContext() {
        // utility class — no instantiation
    }

    /**
     * Populates the {@link SecurityContextHolder} with a {@link UsernamePasswordAuthenticationToken}
     * that carries {@code userId} as principal name and {@code username} as credentials.
     * The token has the {@code ROLE_USER} authority.
     *
     * <p>This is sufficient for controller tests that call
     * {@code SecurityContextHolder.getContext().getAuthentication().getName()}.
     *
     * @param userId   the subject identifier (e.g. a UUID from Keycloak's {@code sub} claim)
     * @param username the display name / preferred_username
     */
    public static void withUser(String userId, String username) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var auth = new UsernamePasswordAuthenticationToken(userId, username, authorities);
        var context = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Populates the {@link SecurityContextHolder} with a {@link JwtAuthenticationToken}
     * that mimics a Keycloak-issued JWT bearer token.
     *
     * <p>Useful for tests that read specific JWT claims from the security context.
     *
     * @param userId   the {@code sub} claim value
     * @param username the {@code preferred_username} claim value
     */
    public static void withJwtUser(String userId, String username) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(userId)
                .claim("preferred_username", username)
                .claim("email", username + "@example.com")
                .build();

        Map<String, Object> claims = jwt.getClaims();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var auth = new JwtAuthenticationToken(jwt, authorities, username);

        var context = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Clears the {@link SecurityContextHolder}.
     * Call this in {@code @AfterEach} to prevent context leakage between tests.
     */
    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
