package com.eshop.catalog.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Received from Ordering when an order transitions to the
 * "awaiting validation" status.
 *
 * <p>Catalog handles this event by checking stock availability for each
 * order line and publishing either
 * {@link OrderStockConfirmedIntegrationEvent} or
 * {@link OrderStockRejectedIntegrationEvent}.
 */
public class OrderStatusChangedToAwaitingValidationIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStatus")
    private String orderStatus;

    @JsonProperty("BuyerName")
    private String buyerName;

    @JsonProperty("OrderStockItems")
    private List<OrderStockItem> orderStockItems;

    // Default constructor for Jackson deserialization
    public OrderStatusChangedToAwaitingValidationIntegrationEvent() {
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
     * Represents a single order line carrying the product id and requested units.
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
