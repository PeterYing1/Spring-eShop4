package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight catalog item DTO used by the aggregator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogItem {

    private int id;
    private String name;
    private BigDecimal price;
    private String pictureUri;
}
