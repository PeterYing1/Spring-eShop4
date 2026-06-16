package com.eshop.payment.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published by the Payment service when payment simulation fails.
 * The Ordering service consumes this event to advance the order to the
 * "payment failed" status.
 */
public class OrderPaymentFailedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    public OrderPaymentFailedIntegrationEvent() {
        super();
    }

    public OrderPaymentFailedIntegrationEvent(int orderId) {
        super();
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
