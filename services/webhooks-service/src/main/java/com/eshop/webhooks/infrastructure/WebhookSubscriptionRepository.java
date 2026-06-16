package com.eshop.webhooks.infrastructure;

import com.eshop.webhooks.domain.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link WebhookSubscription} entities.
 */
public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Integer> {

    /**
     * Returns all subscriptions belonging to the given user.
     *
     * @param userId the subject identifier of the user
     */
    List<WebhookSubscription> findByUserId(String userId);

    /**
     * Returns all subscriptions for the given webhook event type name
     * (e.g. {@code "OrderPaid"}, {@code "ProductPriceChanged"}).
     *
     * @param type the type name stored in the {@code Type} column
     */
    List<WebhookSubscription> findByType(String type);

    /**
     * Returns the subscription with the given {@code id} if it belongs to
     * the specified user, or empty if not found or owned by a different user.
     *
     * @param id     the subscription primary key
     * @param userId the subject identifier of the requesting user
     */
    Optional<WebhookSubscription> findByIdAndUserId(int id, String userId);
}
