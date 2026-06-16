package com.eshop.basket.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload submitted by the client when initiating checkout.
 * Carries shipping address, payment card details, and a buyer reference.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketCheckout {

    private String city;

    private String street;

    private String state;

    private String country;

    private String zipCode;

    private String cardNumber;

    private String cardHolderName;

    private Instant cardExpiration;

    private String cardSecurityNumber;

    private int cardTypeId;

    private String buyer;

    private UUID requestId;
}
