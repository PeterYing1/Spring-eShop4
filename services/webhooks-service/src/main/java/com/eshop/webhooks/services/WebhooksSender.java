package com.eshop.webhooks.services;

import com.eshop.webhooks.domain.WebhookData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Dispatches outbound webhook HTTP requests to subscriber destination URLs.
 *
 * <p>Each call to {@link #send(WebhookData)} performs a synchronous HTTP POST
 * to the subscriber's {@code destUrl} with:
 * <ul>
 *   <li>Body: the JSON-serialised integration event payload.</li>
 *   <li>{@code Content-Type: application/json}</li>
 *   <li>{@code Authorization: eshop-webhooks <token>} (only when a token is set).</li>
 * </ul>
 *
 * <p>Exceptions are caught and logged — this method never throws, so a failure
 * to dispatch one webhook does not affect other subscribers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhooksSender {

    private final RestTemplate restTemplate;

    /**
     * POSTs the webhook payload to the destination URL specified in {@code data}.
     *
     * @param data the webhook delivery descriptor (destination, payload, token)
     */
    public void send(WebhookData data) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (data.getToken() != null && !data.getToken().isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, "eshop-webhooks " + data.getToken());
            }

            HttpEntity<String> request = new HttpEntity<>(data.getPayload(), headers);

            log.debug("Sending webhook type='{}' to '{}'", data.getSubscriptionType(), data.getDestUrl());

            var response = restTemplate.postForEntity(data.getDestUrl(), request, String.class);

            log.info("Webhook type='{}' delivered to '{}' — HTTP {}",
                    data.getSubscriptionType(), data.getDestUrl(),
                    response.getStatusCode().value());

        } catch (Exception ex) {
            log.error("Failed to deliver webhook type='{}' to '{}': {}",
                    data.getSubscriptionType(), data.getDestUrl(), ex.getMessage(), ex);
            // Intentionally not re-thrown — one failed delivery must not block others
        }
    }
}
