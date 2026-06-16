package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Received from the Payment service when payment failed.
 */
public class OrderPaymentFailedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderPaymentFailedIntegrationEvent() {
        super();
    }

    public int getOrderId() { return orderId; }
}
