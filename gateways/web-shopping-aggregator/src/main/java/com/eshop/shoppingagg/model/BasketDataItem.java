package com.eshop.shoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item within a {@link BasketData}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketDataItem {

    private String id;
    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal oldUnitPrice;
    private int quantity;
    private String pictureUrl;
}
