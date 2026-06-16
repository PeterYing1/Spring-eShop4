package com.eshop.webmvc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {
    private String id;
    private int productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private String pictureUrl;
}
