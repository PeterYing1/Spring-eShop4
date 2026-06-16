package com.eshop.catalog.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Published by Catalog when one or more items in an order lack sufficient stock,
 * in response to {@link OrderStatusChangedToAwaitingValidationIntegrationEvent}.
 *
 * <p>Consumed by: Ordering (to cancel or partially fulfil the order).
 */
public class OrderStockRejectedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("OrderId")
    private final int orderId;

    @JsonProperty("OrderStockItems")
    private final List<ConfirmedOrderStockItem> orderStockItems;

    public OrderStockRejectedIntegrationEvent(int orderId, List<ConfirmedOrderStockItem> orderStockItems) {
        super();
        this.orderId = orderId;
        this.orderStockItems = orderStockItems;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<ConfirmedOrderStockItem> getOrderStockItems() {
        return orderStockItems;
    }

    // -------------------------------------------------------------------------
    // Nested record: one entry per order line with a stock verdict
    // -------------------------------------------------------------------------

    /**
     * Stock verdict for a single order line.
     */
    public static class ConfirmedOrderStockItem {

        @JsonProperty("ProductId")
        private final int productId;

        @JsonProperty("HasStock")
        private final boolean hasStock;

        public ConfirmedOrderStockItem(int productId, boolean hasStock) {
            this.productId = productId;
            this.hasStock = hasStock;
        }

        public int getProductId() {
            return productId;
        }

        public boolean isHasStock() {
            return hasStock;
        }
    }
}
