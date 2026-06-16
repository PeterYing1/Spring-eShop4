package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A quantity update for a single basket line.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketItemData {

    private String basketItemId;
    private int newQty;
}
