package com.eshop.webhooks.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Value object carrying the data needed to dispatch a single webhook delivery.
 *
 * <p>Instances are created by the integration event handlers and passed to
 * {@link com.eshop.webhooks.services.WebhooksSender} for HTTP dispatch.
 */
@Data
@AllArgsConstructor
public class WebhookData {

    /**
     * The webhook event type name (e.g. {@code "OrderPaid"}).
     */
    private String subscriptionType;

    /**
     * The JSON-serialised integration event payload to POST to the subscriber.
     */
    private String payload;

    /**
     * The destination URL of the webhook subscriber.
     */
    private String destUrl;

    /**
     * Optional shared secret token; sent as {@code Authorization: eshop-webhooks <token>}.
     */
    private String token;
}
