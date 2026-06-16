package com.eshop.webhooks.domain;

import java.util.Arrays;

/**
 * Enumeration of supported webhook event types.
 *
 * <p>The {@code typeName} string is the value stored in the
 * {@code WebhookSubscriptions.Type} column and used in webhook payloads.
 */
public enum WebhookType {

    ORDER_PAID("OrderPaid"),
    PRODUCT_PRICE_CHANGED("ProductPriceChanged");

    private final String typeName;

    WebhookType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * Looks up a {@link WebhookType} by its {@code typeName} string (case-insensitive).
     *
     * @param name the type name to look up (e.g. {@code "OrderPaid"})
     * @return the matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static WebhookType fromName(String name) {
        return Arrays.stream(values())
                .filter(wt -> wt.typeName.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown WebhookType: " + name));
    }
}
