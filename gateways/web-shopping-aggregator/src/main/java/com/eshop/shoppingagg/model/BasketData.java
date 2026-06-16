package com.eshop.shoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's shopping basket as returned/accepted by the basket
 * service and surfaced through this BFF aggregator.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketData {

    private String buyerId;
    private List<BasketDataItem> items = new ArrayList<>();

    public BasketData(String buyerId) {
        this.buyerId = buyerId;
    }
}
