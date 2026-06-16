package com.eshop.orderingbackground.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration event published when the grace period for an order has expired.
 *
 * <p>When the ordering background task detects an order in {@code submitted}
 * status (statusId = 1) whose {@code OrderDate} is older than the configured
 * grace period, it publishes this event so that the ordering service can
 * transition the order to {@code awaitingvalidation} status.
 */
public class GracePeriodConfirmedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private final int orderId;

    public GracePeriodConfirmedIntegrationEvent(int orderId) {
        super();
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
