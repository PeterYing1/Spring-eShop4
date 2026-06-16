package com.eshop.webhookclient.web.controllers;

import com.eshop.webhookclient.config.WebhookClientProperties;
import com.eshop.webhookclient.model.ReceivedWebhook;
import com.eshop.webhookclient.services.ReceivedWebhookStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller that receives incoming webhook POST callbacks from the webhooks-service.
 * This endpoint is publicly accessible (no user session required) and exempt from CSRF protection.
 *
 * <p>The webhooks-service sends an {@code Authorization: eshop-webhooks <token>} header.
 * The token is validated against {@code webhook-client.token} from application.yml.</p>
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookReceiverController {

    /** Header prefix used by the eShop webhooks-service. */
    private static final String TOKEN_PREFIX = "eshop-webhooks ";

    private final ReceivedWebhookStore receivedWebhookStore;
    private final WebhookClientProperties webhookClientProperties;

    /**
     * Receives a webhook callback payload.
     *
     * @param authorizationHeader The {@code Authorization} header value (e.g. {@code eshop-webhooks my-secret-token}).
     * @param payload             Raw JSON payload sent by the webhooks-service.
     */
    @PostMapping("/receive")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody String payload) {

        String receivedToken = extractToken(authorizationHeader);
        String configuredToken = webhookClientProperties.getToken();

        log.info("Received webhook callback. Token present: {}, configured token: {}",
                receivedToken != null ? "yes" : "no", configuredToken);

        if (!isTokenValid(receivedToken, configuredToken)) {
            log.warn("Webhook callback rejected: token mismatch. Received='{}', Expected='{}'",
                    receivedToken, configuredToken);
            return ResponseEntity.badRequest().build();
        }

        // Attempt to extract the event type from the raw JSON payload (naive extraction)
        String type = extractType(payload);

        ReceivedWebhook webhook = new ReceivedWebhook(type, payload, Instant.now());
        receivedWebhookStore.add(webhook);

        log.info("Webhook callback stored. Type='{}', Payload length={}", type, payload.length());
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Strips the {@code "eshop-webhooks "} prefix from the Authorization header.
     *
     * @return the bare token string, or {@code null} if the header is absent or malformed.
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length()).trim();
        }
        // Also handle plain Bearer tokens (defensive)
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }
        return authorizationHeader.trim();
    }

    /**
     * Validates the incoming token against the configured secret.
     * If the configured token is blank, any token is accepted (validation disabled).
     */
    private boolean isTokenValid(String receivedToken, String configuredToken) {
        if (configuredToken == null || configuredToken.isBlank()) {
            return true; // Validation disabled
        }
        return configuredToken.equals(receivedToken);
    }

    /**
     * Naively extracts the "type" field from a JSON string.
     * Falls back to "Unknown" if not found.
     */
    private String extractType(String json) {
        if (json == null) {
            return "Unknown";
        }
        // Look for "type":"value" or "Type":"value"
        for (String key : new String[]{"\"type\":", "\"Type\":"}) {
            int idx = json.indexOf(key);
            if (idx >= 0) {
                int start = json.indexOf('"', idx + key.length());
                if (start >= 0) {
                    int end = json.indexOf('"', start + 1);
                    if (end > start) {
                        return json.substring(start + 1, end);
                    }
                }
            }
        }
        return "Unknown";
    }
}
