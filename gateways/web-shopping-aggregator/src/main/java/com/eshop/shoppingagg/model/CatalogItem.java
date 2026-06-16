package com.eshop.shoppingagg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Minimal projection of a catalog item used internally by this aggregator.
 * Populated by calling {@code GET /api/v1/catalog/items/{id}} on the catalog
 * service.
 */
@Data
@NoArgsConstructor
public class CatalogItem {

    private int id;
    private String name;
    private BigDecimal price;
    private String pictureUri;
}
