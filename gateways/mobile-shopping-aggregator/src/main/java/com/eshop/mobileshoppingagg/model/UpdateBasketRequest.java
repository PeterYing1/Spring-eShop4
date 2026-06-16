package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for POST/PUT /api/v1/basket — replaces the whole basket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBasketRequest {

    private String buyerId;
    private List<UpdateBasketRequestItemData> items;
}
