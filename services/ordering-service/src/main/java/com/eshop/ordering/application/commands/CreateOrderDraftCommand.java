package com.eshop.ordering.application.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command to create a draft order preview from basket contents.
 *
 * <p>The draft is not persisted; it is used solely to compute totals and
 * return the {@code OrderDraftDTO} to the client.
 */
public class CreateOrderDraftCommand {

    @JsonProperty("buyerId")
    private String buyerId;

    @JsonProperty("items")
    private List<BasketItemDTO> items;

    public CreateOrderDraftCommand() {
    }

    public CreateOrderDraftCommand(String buyerId, List<BasketItemDTO> items) {
        this.buyerId = buyerId;
        this.items = items;
    }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public List<BasketItemDTO> getItems() { return items; }
    public void setItems(List<BasketItemDTO> items) { this.items = items; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * A single basket item carried in the draft command.
     */
    public static class BasketItemDTO {

        @JsonProperty("id")
        private String id;

        @JsonProperty("productId")
        private int productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("unitPrice")
        private BigDecimal unitPrice;

        @JsonProperty("oldUnitPrice")
        private BigDecimal oldUnitPrice;

        @JsonProperty("quantity")
        private int quantity;

        @JsonProperty("pictureUrl")
        private String pictureUrl;

        public BasketItemDTO() {
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getOldUnitPrice() { return oldUnitPrice; }
        public void setOldUnitPrice(BigDecimal oldUnitPrice) { this.oldUnitPrice = oldUnitPrice; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
