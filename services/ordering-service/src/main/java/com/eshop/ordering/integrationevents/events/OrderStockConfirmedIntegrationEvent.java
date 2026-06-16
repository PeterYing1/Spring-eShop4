package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Received from the Catalog service when all order items have sufficient stock.
 */
public class OrderStockConfirmedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderStockConfirmedIntegrationEvent() {
        super();
    }

    public int getOrderId() { return orderId; }
}
