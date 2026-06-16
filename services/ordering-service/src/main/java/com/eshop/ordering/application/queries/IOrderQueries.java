package com.eshop.ordering.application.queries;

import java.util.List;
import java.util.UUID;

/**
 * Query-side read model interface for orders.
 *
 * <p>Implementations use raw SQL via {@code JdbcTemplate} for efficient
 * multi-table queries without going through the full JPA entity graph.
 */
public interface IOrderQueries {

    /**
     * Returns the full detail view of a single order.
     *
     * @param orderId the order primary key
     * @return the order view model
     * @throws java.util.NoSuchElementException if not found
     */
    OrderViewModel getOrder(int orderId);

    /**
     * Returns summary rows for all orders belonging to the given user.
     *
     * @param userId the identity subject of the user
     * @return list of order summaries; never {@code null}
     */
    List<OrderSummary> getOrdersFromUser(UUID userId);

    /**
     * Returns all known card types.
     *
     * @return list of card types
     */
    List<CardType> getCardTypes();
}
