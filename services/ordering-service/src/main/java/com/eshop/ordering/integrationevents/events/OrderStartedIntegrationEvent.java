package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published when an order is first created.
 *
 * <p>Consumed by the Basket service to clear the user's basket.
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
