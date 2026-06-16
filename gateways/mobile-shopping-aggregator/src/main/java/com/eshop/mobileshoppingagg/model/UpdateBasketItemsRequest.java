package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for PUT /api/v1/basket/items — updates individual item quantities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketItemsRequest {

    private String basketId;
    private List<UpdateBasketItemData> updates = new ArrayList<>();
}
