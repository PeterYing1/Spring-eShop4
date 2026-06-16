package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published when an order transitions to STOCK_CONFIRMED status.
 *
 * <p>Consumed by the Payment service to initiate payment.
 */
public class OrderStatusChangedToStockConfirmedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderStatusChangedToStockConfirmedIntegrationEvent() {
        super();
    }

    public OrderStatusChangedToStockConfirmedIntegrationEvent(int orderId) {
        super();
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
}
