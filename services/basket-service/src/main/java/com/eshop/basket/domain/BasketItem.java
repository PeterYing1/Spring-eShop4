package com.eshop.basket.domain;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item inside a {@link CustomerBasket}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {

    private String id;

    private int productId;

    private String productName;

    private BigDecimal unitPrice;

    private BigDecimal oldUnitPrice;

    @Min(value = 1, message = "Invalid number of units")
    private int quantity;

    private String pictureUrl;
}
