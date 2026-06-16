package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Order draft / order details returned to the mobile client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderData {

    private String orderNumber;
    private Instant date;
    private String status;
    private BigDecimal total;
    private String description;
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private String cardNumber;
    private String cardHolderName;
    private boolean isDraft;
    private Instant cardExpiration;
    private String cardExpirationShort;
    private String cardSecurityNumber;
    private int cardTypeId;
    private String buyer;
    private List<OrderDataItem> orderItems = new ArrayList<>();
}
