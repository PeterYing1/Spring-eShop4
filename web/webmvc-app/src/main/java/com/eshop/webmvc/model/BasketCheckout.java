package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String cardExpiration;
    private String cardSecurityNumber;
    private int cardTypeId;
    private String buyer;
}
