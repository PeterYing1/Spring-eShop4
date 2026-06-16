package com.eshop.basket.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping basket for a single buyer.
 * Stored as JSON in Redis, keyed by {@code buyerId}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBasket {

    private String buyerId;

    private List<BasketItem> items = new ArrayList<>();

    public CustomerBasket(String buyerId) {
        this.buyerId = buyerId;
    }
}
