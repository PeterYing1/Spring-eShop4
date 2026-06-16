package com.eshop.orderinghub.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration event published by the Ordering service when an order transitions
 * to the <em>Submitted</em> status.
 */
public class OrderStatusChangedToSubmittedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    public OrderStatusChangedToSubmittedIntegrationEvent() {
    }

    public OrderStatusChangedToSubmittedIntegrationEvent(int orderId, String orderStatus, String buyerName) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
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
}
