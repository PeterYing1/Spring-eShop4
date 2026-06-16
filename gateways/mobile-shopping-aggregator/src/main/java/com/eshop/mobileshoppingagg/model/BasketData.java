package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the basket stored/retrieved from the basket-service.
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
