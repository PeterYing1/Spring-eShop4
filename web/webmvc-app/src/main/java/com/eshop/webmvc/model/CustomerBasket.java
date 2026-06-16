package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CustomerBasket {
    private String buyerId;
    private List<BasketItem> items = new ArrayList<>();
}
