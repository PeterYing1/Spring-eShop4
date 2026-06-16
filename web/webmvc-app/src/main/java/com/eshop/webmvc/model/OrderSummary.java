package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class OrderSummary {
    private int ordernumber;
    private Instant date;
    private String status;
    private BigDecimal total;
}
