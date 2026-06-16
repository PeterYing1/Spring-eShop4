package com.eshop.webhooks.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Received from the Ordering service when an order transitions to the "paid" status.
 *
 * <p>The Webhooks service handles this event by dispatching webhook payloads to
 * all subscribers registered for the {@code OrderPaid} event type.
 *
 * <p>Property names use {@code @JsonProperty} PascalCase to match the .NET
 * Newtonsoft.Json serialisation convention.
 */
public class OrderStatusChangedToPaidIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    @JsonProperty("OrderDate")
    private Instant orderDate;

    /** Default constructor for Jackson deserialisation. */
    public OrderStatusChangedToPaidIntegrationEvent() {
        super();
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public Instant getOrderDate() {
        return orderDate;
    }
}
