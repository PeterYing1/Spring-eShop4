package com.eshop.basket.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Published by the Catalog service when a product's price changes.
 * The Basket service consumes this event to update any basket that
 * contains the affected product.
 */
public class ProductPriceChangedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("ProductId")
    private int productId;

    @JsonProperty("NewPrice")
    private BigDecimal newPrice;

    @JsonProperty("OldPrice")
    private BigDecimal oldPrice;

    public ProductPriceChangedIntegrationEvent() {
        super();
    }

    public ProductPriceChangedIntegrationEvent(int productId, BigDecimal newPrice, BigDecimal oldPrice) {
        super();
        this.productId = productId;
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
    }

    public int getProductId() { return productId; }
    public BigDecimal getNewPrice() { return newPrice; }
    public BigDecimal getOldPrice() { return oldPrice; }
}
