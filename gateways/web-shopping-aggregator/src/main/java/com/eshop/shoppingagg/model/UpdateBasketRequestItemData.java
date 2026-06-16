package com.eshop.shoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single product-quantity pair inside an {@link UpdateBasketRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketRequestItemData {

    /** Basket-item identifier (UUID string). May be blank for new items. */
    private String id;
    private int productId;
    private int quantity;
}
