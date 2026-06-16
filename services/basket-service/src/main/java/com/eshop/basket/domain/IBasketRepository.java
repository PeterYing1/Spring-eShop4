package com.eshop.basket.domain;

/**
 * Repository abstraction for {@link CustomerBasket} persistence.
 * The primary store is Redis (JSON strings keyed by {@code buyerId}).
 */
public interface IBasketRepository {

    /**
     * Retrieves the basket for the given customer.
     *
     * @param customerId the buyer / user identifier
     * @return the basket, or {@code null} if none exists
     */
    CustomerBasket getBasket(String customerId);

    /**
     * Persists (creates or replaces) the basket and returns the saved state.
     *
     * @param basket basket to persist
     * @return the stored basket
     */
    CustomerBasket updateBasket(CustomerBasket basket);

    /**
     * Removes the basket for the given identifier.
     *
     * @param id the buyer / user identifier whose basket should be deleted
     */
    void deleteBasket(String id);
}
