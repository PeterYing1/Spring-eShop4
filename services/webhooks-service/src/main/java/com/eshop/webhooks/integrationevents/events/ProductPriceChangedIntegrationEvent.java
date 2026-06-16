package com.eshop.webhooks.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Received from the Catalog service when the price of a product changes.
 *
 * <p>The Webhooks service handles this event by dispatching webhook payloads to
 * all subscribers registered for the {@code ProductPriceChanged} event type.
 *
 * <p>Property names use {@code @JsonProperty} PascalCase to match the .NET
 * Newtonsoft.Json serialisation convention.
 */
public class ProductPriceChangedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("ProductId")
    private int productId;

    @JsonProperty("NewPrice")
    private BigDecimal newPrice;

    @JsonProperty("OldPrice")
    private BigDecimal oldPrice;

    /** Default constructor for Jackson deserialisation. */
    public ProductPriceChangedIntegrationEvent() {
        super();
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
