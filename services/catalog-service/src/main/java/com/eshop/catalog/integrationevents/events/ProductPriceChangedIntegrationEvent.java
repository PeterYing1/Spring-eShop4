package com.eshop.catalog.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Published when the price of a catalog item changes via
 * {@code PUT /api/v1/catalog/items}.
 *
 * <p>Consumed by: Basket (to update {@code oldUnitPrice}), Webhooks.
 *
 * <p>Property names use PascalCase via {@code @JsonProperty} to match
 * the .NET Newtonsoft.Json serialisation convention.
 */
public class ProductPriceChangedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("ProductId")
    private final int productId;

    @JsonProperty("NewPrice")
    private final BigDecimal newPrice;

    @JsonProperty("OldPrice")
    private final BigDecimal oldPrice;

    public ProductPriceChangedIntegrationEvent(int productId, BigDecimal newPrice, BigDecimal oldPrice) {
        super();
        this.productId = productId;
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
    }

    public int getProductId() {
        return productId;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }
}
