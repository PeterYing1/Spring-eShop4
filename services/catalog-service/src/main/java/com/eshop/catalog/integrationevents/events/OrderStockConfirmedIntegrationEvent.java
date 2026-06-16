package com.eshop.catalog.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published by Catalog when ALL items in an order have sufficient stock,
 * in response to {@link OrderStatusChangedToAwaitingValidationIntegrationEvent}.
 *
 * <p>Consumed by: Ordering (to advance order to stock-confirmed status).
 */
public class OrderStockConfirmedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private final int orderId;

    public OrderStockConfirmedIntegrationEvent(int orderId) {
        super();
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
