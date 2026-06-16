package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderItemSummary {
    private String productname;
    private int units;
    private BigDecimal unitprice;
    private String pictureurl;
}
