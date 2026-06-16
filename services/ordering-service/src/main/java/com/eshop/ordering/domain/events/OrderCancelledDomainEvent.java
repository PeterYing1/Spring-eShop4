package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregatesmodel.order.Order;

/**
 * Raised when an order transitions to the {@code CANCELLED} status.
 */
public class OrderCancelledDomainEvent {

    private final Order order;

    public OrderCancelledDomainEvent(Order order) {
        this.order = order;
    }

    public Order getOrder() { return order; }
}
