package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single item entry inside {@link UpdateBasketRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketRequestItemData {

    /** Basket item id. */
    private String id;

    /** Catalog item id. */
    private int productId;

    /** Desired quantity. */
    private int quantity;
}
