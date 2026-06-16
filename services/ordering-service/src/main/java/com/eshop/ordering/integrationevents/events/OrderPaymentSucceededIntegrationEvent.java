package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Received from the Payment service when payment was successfully processed.
 */
public class OrderPaymentSucceededIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderPaymentSucceededIntegrationEvent() {
        super();
    }

    public int getOrderId() { return orderId; }
}
