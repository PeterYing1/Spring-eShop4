package com.eshop.webhooks.services;

import com.eshop.webhooks.domain.WebhookSubscription;
import com.eshop.webhooks.infrastructure.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for querying {@link WebhookSubscription} records.
 *
 * <p>Provides lookup by event type name (used by integration event handlers)
 * and by user identifier (used by the REST controller).
 */
@Service
@RequiredArgsConstructor
public class WebhooksRetriever {

    private final WebhookSubscriptionRepository repository;

    /**
     * Returns all subscriptions for the given webhook event type name.
     *
     * @param type the type name (e.g. {@code "OrderPaid"}, {@code "ProductPriceChanged"})
     * @return a (possibly empty) list of matching subscriptions
     */
    public List<WebhookSubscription> getSubscriptions(String type) {
        return repository.findByType(type);
    }

    /**
     * Returns all subscriptions belonging to the given user.
     *
     * @param userId the subject identifier of the user
     * @return a (possibly empty) list of the user's subscriptions
     */
    public List<WebhookSubscription> getUserSubscriptions(String userId) {
        return repository.findByUserId(userId);
    }
}
