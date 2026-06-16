package com.eshop.ordering.domain.events;

/**
 * Raised when an order transitions to the {@code STOCK_CONFIRMED} status.
 */
public class OrderStatusChangedToStockConfirmedDomainEvent {

    private final int orderId;

    public OrderStatusChangedToStockConfirmedDomainEvent(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
}
