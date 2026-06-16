package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item inside an {@link OrderData}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDataItem {

    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private int units;
    private String pictureUrl;
}
