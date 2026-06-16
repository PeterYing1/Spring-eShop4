package com.eshop.orderinghub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * STOMP channel interceptor that validates the JWT carried in the
 * {@code Authorization} header of STOMP {@code CONNECT} frames.
 *
 * <p>When a client connects it must pass:
 * <pre>
 * CONNECT
 * Authorization: Bearer &lt;access-token&gt;
 * ...
 * </pre>
 *
 * <p>If the token is valid, a {@link UsernamePasswordAuthenticationToken} is
 * set on the STOMP session header so that Spring's
 * {@code SimpMessagingTemplate.convertAndSendToUser} can route messages by the
 * JWT {@code sub} or {@code preferred_username} claim.
 */
@Slf4j
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    public JwtChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Only process CONNECT frames; pass everything else through unchanged
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("STOMP CONNECT frame has no Bearer token — proceeding as anonymous");
            return message;
        }

        String tokenValue = authHeader.substring(BEARER_PREFIX.length());
        try {
            Jwt jwt = jwtDecoder.decode(tokenValue);

            // Prefer preferred_username (Keycloak convention); fall back to subject
            String username = jwt.hasClaim("preferred_username")
                    ? jwt.getClaimAsString("preferred_username")
                    : jwt.getSubject();

            List<SimpleGrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            accessor.setUser(authentication);
            log.debug("STOMP CONNECT authenticated as user: {}", username);
        } catch (JwtException ex) {
            log.warn("STOMP CONNECT rejected — invalid JWT: {}", ex.getMessage());
            // Return message without setting a user; downstream security will reject
            // unauthenticated subscriptions to protected destinations if configured.
        }

        return message;
    }
}
