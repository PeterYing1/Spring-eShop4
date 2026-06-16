package com.eshop.basket.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published by the Ordering service when an order has been started (accepted).
 * The Basket service consumes this event to delete the buyer's basket.
 */
public class OrderStartedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("UserId")
    private String userId;

    public OrderStartedIntegrationEvent() {
        super();
    }

    public OrderStartedIntegrationEvent(String userId) {
        super();
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}
