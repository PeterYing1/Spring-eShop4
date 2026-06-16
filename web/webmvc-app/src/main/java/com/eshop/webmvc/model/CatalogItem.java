package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CatalogItem {
    private int id;
    private String name;
    private BigDecimal price;
    private String pictureUri;
    private int catalogTypeId;
    private int catalogBrandId;
}
