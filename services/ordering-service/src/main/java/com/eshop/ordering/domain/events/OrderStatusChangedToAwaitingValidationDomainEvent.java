package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregatesmodel.order.OrderItem;

import java.util.List;

/**
 * Raised when an order transitions to the
 * {@code AWAITING_VALIDATION} status.
 */
public class OrderStatusChangedToAwaitingValidationDomainEvent {

    private final int orderId;
    private final List<OrderItem> orderItems;

    public OrderStatusChangedToAwaitingValidationDomainEvent(int orderId, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.orderItems = List.copyOf(orderItems);
    }

    public int getOrderId() { return orderId; }
    public List<OrderItem> getOrderItems() { return orderItems; }
}
