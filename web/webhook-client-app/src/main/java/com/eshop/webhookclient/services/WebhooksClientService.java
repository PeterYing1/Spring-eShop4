package com.eshop.webhookclient.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client service that calls the webhooks-api REST endpoints.
 */
@Slf4j
@Service
public class WebhooksClientService {

    private final RestTemplate webhooksRestTemplate;

    public WebhooksClientService(@Qualifier("webhooksRestTemplate") RestTemplate webhooksRestTemplate) {
        this.webhooksRestTemplate = webhooksRestTemplate;
    }

    /**
     * Returns the list of webhook subscriptions for the authenticated user.
     *
     * @param accessToken Bearer token obtained from the current OIDC session.
     */
    public List<Map<String, Object>> getSubscriptions(String accessToken) {
        HttpHeaders headers = bearerHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = webhooksRestTemplate.exchange(
                    "/api/v1/webhooks/",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> body = response.getBody();
            return body != null ? body : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to load webhook subscriptions: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Subscribes to a webhook event type.
     *
     * @return true if the subscription was created successfully.
     */
    public boolean subscribe(String type, String url, String token, String accessToken) {
        HttpHeaders headers = bearerHeaders(accessToken);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        Map<String, String> body = Map.of(
                "type", type,
                "url", url,
                "token", token
        );
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = webhooksRestTemplate.exchange(
                    "/api/v1/webhooks/",
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            log.info("Subscribe response status: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.error("Failed to subscribe to webhook type '{}': {}", type, ex.getMessage());
            return false;
        }
    }

    /**
     * Deletes (unsubscribes) a webhook subscription by its ID.
     *
     * @return true if the deletion was successful.
     */
    public boolean unsubscribe(int id, String accessToken) {
        HttpHeaders headers = bearerHeaders(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = webhooksRestTemplate.exchange(
                    "/api/v1/webhooks/" + id,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            log.info("Unsubscribe response status: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.error("Failed to unsubscribe webhook id {}: {}", id, ex.getMessage());
            return false;
        }
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null && !accessToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return headers;
    }
}
