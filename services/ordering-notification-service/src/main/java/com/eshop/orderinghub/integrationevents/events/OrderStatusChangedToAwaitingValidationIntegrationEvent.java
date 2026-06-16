package com.eshop.orderinghub.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration event published by the Ordering service when an order transitions
 * to the <em>Awaiting Validation</em> status.
 *
 * <p>Property names use PascalCase {@code @JsonProperty} annotations to match
 * the .NET Newtonsoft.Json serialisation convention used by the publisher.
 */
public class OrderStatusChangedToAwaitingValidationIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    /** Default constructor required for Jackson deserialization. */
    public OrderStatusChangedToAwaitingValidationIntegrationEvent() {
    }

    public OrderStatusChangedToAwaitingValidationIntegrationEvent(int orderId, String orderStatus, String buyerName) {
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
