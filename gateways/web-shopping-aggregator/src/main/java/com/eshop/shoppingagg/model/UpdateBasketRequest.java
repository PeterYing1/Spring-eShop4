package com.eshop.shoppingagg.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for {@code POST /api/v1/basket} — replaces the entire basket
 * contents for the given buyer.
 */
@Data
@NoArgsConstructor
public class UpdateBasketRequest {

    private String buyerId;
    private List<UpdateBasketRequestItemData> items;
}
