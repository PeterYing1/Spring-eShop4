package com.eshop.webhooks.api;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/webhooks} — creates a new webhook subscription.
 */
@Data
@NoArgsConstructor
public class WebhookSubscriptionRequest {

    /**
     * The webhook event type name to subscribe to
     * (e.g. {@code "OrderPaid"}, {@code "ProductPriceChanged"}).
     */
    private String type;

    /**
     * The destination URL to which the webhook payload will be POSTed.
     */
    private String url;

    /**
     * Optional shared secret token included in the {@code Authorization} header
     * of each outbound webhook request.
     */
    private String token;
}
