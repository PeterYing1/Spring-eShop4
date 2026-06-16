package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item in a {@link BasketData}.
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
