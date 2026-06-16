package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderDetails {
    private int ordernumber;
    private Instant date;
    private String status;
    private String description;
    private String street;
    private String city;
    private String zipcode;
    private String country;
    private List<OrderItemSummary> orderitems;
    private BigDecimal total;
}
