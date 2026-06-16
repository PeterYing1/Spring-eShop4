package com.eshop.payment.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published by the Ordering service when an order's stock has been confirmed
 * by the Catalog service.  The Payment service consumes this event to trigger
 * payment simulation.
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

    public int getOrderId() {
        return orderId;
    }
}
