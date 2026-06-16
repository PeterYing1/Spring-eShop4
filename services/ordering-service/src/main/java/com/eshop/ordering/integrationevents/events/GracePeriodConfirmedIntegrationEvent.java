package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Received from the Ordering Background service when the grace period for
 * an order has expired and the order is ready for stock validation.
 */
public class GracePeriodConfirmedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public GracePeriodConfirmedIntegrationEvent() {
        super();
    }

    public int getOrderId() { return orderId; }
}
