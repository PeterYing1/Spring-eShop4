package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class BasketCheckout {
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private String cardNumber;
    private String cardHolderName;
    private String cardExpiration; // ISO-8601 instant string sent to basket API
    private String cardSecurityNumber;
    private int cardTypeId;
    private String buyer;
    private UUID requestId;
}
