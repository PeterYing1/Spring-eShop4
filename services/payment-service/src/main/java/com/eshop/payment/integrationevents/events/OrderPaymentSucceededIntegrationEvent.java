package com.eshop.payment.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published by the Payment service when payment simulation succeeds.
 * The Ordering service consumes this event to advance the order to the
 * "paid" status.
 */
public class OrderPaymentSucceededIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderPaymentSucceededIntegrationEvent() {
        super();
    }

    public OrderPaymentSucceededIntegrationEvent(int orderId) {
        super();
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
