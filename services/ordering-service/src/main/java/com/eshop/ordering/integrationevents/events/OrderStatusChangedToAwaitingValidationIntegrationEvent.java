package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Published when an order transitions to the AWAITING_VALIDATION status.
 *
 * <p>Consumed by the Catalog service to check stock.
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

    public OrderStatusChangedToAwaitingValidationIntegrationEvent() {
        super();
    }

    public OrderStatusChangedToAwaitingValidationIntegrationEvent(
            int orderId, String orderStatus, String buyerName,
            List<OrderStockItem> orderStockItems) {
        super();
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.buyerName = buyerName;
        this.orderStockItems = orderStockItems;
    }

    public int getOrderId() { return orderId; }
    public String getOrderStatus() { return orderStatus; }
    public String getBuyerName() { return buyerName; }
    public List<OrderStockItem> getOrderStockItems() { return orderStockItems; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * Product id + units for one order line.
     */
    public static class OrderStockItem {

        @JsonProperty("ProductId")
        private int productId;

        @JsonProperty("Units")
        private int units;

        public OrderStockItem() {
        }

        public OrderStockItem(int productId, int units) {
            this.productId = productId;
            this.units = units;
        }

        public int getProductId() { return productId; }
        public int getUnits() { return units; }
    }
}
