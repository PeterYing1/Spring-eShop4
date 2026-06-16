package com.eshop.mobileshoppingagg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/v1/basket/items — appends a new item to the basket
 * (mobile BFF append-only behaviour; does NOT merge with existing lines).
 */
@Data
@NoArgsConstructor
public class AddBasketItemRequest {

    private int catalogItemId;
    private String basketId;
    private int quantity = 1;
}
