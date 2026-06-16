package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published when an order transitions to SHIPPED status.
 */
public class OrderStatusChangedToShippedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    public OrderStatusChangedToShippedIntegrationEvent() {
        super();
    }

    public OrderStatusChangedToShippedIntegrationEvent(int orderId, String orderStatus, String buyerName) {
        super();
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
    }

    public int getOrderId() { return orderId; }
    public String getOrderStatus() { return orderStatus; }
    public String getBuyerName() { return buyerName; }
}
