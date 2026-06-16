package com.eshop.webhooks.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a webhook subscription stored in {@code WebhookSubscriptions}.
 *
 * <p>A subscription links a user to a specific webhook event type and a destination URL
 * to which the webhook payload will be POSTed when the event fires.
 */
@Entity
@Table(name = "WebhookSubscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * The webhook event type name (e.g. {@code "OrderPaid"}, {@code "ProductPriceChanged"}).
     */
    @Column(name = "Type", nullable = false, length = 50)
    private String type;

    /**
     * The subject identifier ({@code sub} claim) of the subscribing user.
     */
    @Column(name = "UserId", nullable = false, length = 255)
    private String userId;

    /**
     * The destination URL to which the webhook payload will be POSTed.
     */
    @Column(name = "DestUrl", nullable = false, length = 500)
    private String destUrl;

    /**
     * Optional shared secret token sent in the {@code Authorization} header
     * of each outbound webhook request.
     */
    @Column(name = "Token", length = 255)
    private String token;

    /**
     * The UTC instant at which this subscription was created.
     */
    @Column(name = "CreatedAt", nullable = false)
    private Instant createdAt;
}
