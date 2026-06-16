package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregatesmodel.order.Order;

/**
 * Raised when an order transitions to the {@code SHIPPED} status.
 */
public class OrderShippedDomainEvent {

    private final Order order;

    public OrderShippedDomainEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() { return order; }
}
