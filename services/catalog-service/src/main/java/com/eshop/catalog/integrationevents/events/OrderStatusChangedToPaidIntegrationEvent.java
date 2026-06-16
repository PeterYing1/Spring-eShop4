package com.eshop.catalog.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Received from Ordering when an order transitions to the "paid" status.
 *
 * <p>Catalog handles this event by calling {@code removeStock} on each
 * catalog item to deduct the sold units from available inventory.
 */
public class OrderStatusChangedToPaidIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    @JsonProperty("OrderStockItems")
    private List<OrderStockItem> orderStockItems;

    // Default constructor for Jackson deserialization
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

    public List<OrderStockItem> getOrderStockItems() {
        return orderStockItems;
    }

    // -------------------------------------------------------------------------
    // Nested class: one entry per order line
    // -------------------------------------------------------------------------

    /**
     * Represents a single order line with product id and units sold.
     */
    public static class OrderStockItem {

        @JsonProperty("ProductId")
        private int productId;

        @JsonProperty("Units")
        private int units;

        // Default constructor for Jackson
        public OrderStockItem() {}

        public int getProductId() {
            return productId;
        }

        public int getUnits() {
            return units;
        }
    }
}
