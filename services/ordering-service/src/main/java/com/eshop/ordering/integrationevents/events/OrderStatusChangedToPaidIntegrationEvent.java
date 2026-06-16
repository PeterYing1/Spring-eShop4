package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Published when an order transitions to PAID status.
 *
 * <p>Consumed by the Catalog service to decrement stock and by the Notification
 * service to inform the buyer.
 */
public class OrderStatusChangedToPaidIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("BuyerName")
    private String buyerName;

    @JsonProperty("OrderDate")
    private Instant orderDate;

    public OrderStatusChangedToPaidIntegrationEvent() {
        super();
    }

    public OrderStatusChangedToPaidIntegrationEvent(int orderId, String buyerName, Instant orderDate) {
        super();
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.orderDate = orderDate;
    }

    public int getOrderId() { return orderId; }
    public String getBuyerName() { return buyerName; }
    public Instant getOrderDate() { return orderDate; }
}
