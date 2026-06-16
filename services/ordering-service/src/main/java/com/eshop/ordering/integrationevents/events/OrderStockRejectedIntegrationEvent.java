package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Received from the Catalog service when one or more order items lack stock.
 */
public class OrderStockRejectedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private int orderId;

    @JsonProperty("OrderStockItems")
    private List<ConfirmedOrderStockItem> orderStockItems;

    public OrderStockRejectedIntegrationEvent() {
        super();
    }

    public int getOrderId() { return orderId; }
    public List<ConfirmedOrderStockItem> getOrderStockItems() { return orderStockItems; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * Carries the result of a stock check for one product.
     */
    public static class ConfirmedOrderStockItem {

        @JsonProperty("ProductId")
        private int productId;

        @JsonProperty("HasStock")
        private boolean hasStock;

        public ConfirmedOrderStockItem() {
        }

        public int getProductId() { return productId; }
        public boolean isHasStock() { return hasStock; }
    }
}
