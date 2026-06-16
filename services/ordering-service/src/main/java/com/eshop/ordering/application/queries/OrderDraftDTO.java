package com.eshop.ordering.application.queries;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO returned by {@code POST /api/v1/orders/draft}.
 *
 * <p>Contains a list of order items and the computed total — not persisted.
 */
public class OrderDraftDTO {

    private List<OrderItemDTO> orderItems;
    private BigDecimal total;

    public OrderDraftDTO() {
    }

    public OrderDraftDTO(List<OrderItemDTO> orderItems, BigDecimal total) {
        this.orderItems = orderItems;
        this.total = total;
    }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * A single order line item within a draft order.
     */
    public static class OrderItemDTO {

        private int productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private int units;
        private String pictureUrl;

        public OrderItemDTO() {
        }

        public OrderItemDTO(int productId, String productName, BigDecimal unitPrice,
                            BigDecimal discount, int units, String pictureUrl) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.units = units;
            this.pictureUrl = pictureUrl;
        }

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }

        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }

        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
