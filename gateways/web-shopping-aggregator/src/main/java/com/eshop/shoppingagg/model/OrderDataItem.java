package com.eshop.shoppingagg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single line item within an {@link OrderData}.
 *
 * <p>Property names match the lowercase JSON contract produced by the ordering
 * service.
 */
@Data
@NoArgsConstructor
public class OrderDataItem {

    @JsonProperty("productname")
    private String productname;

    @JsonProperty("units")
    private int units;

    @JsonProperty("unitprice")
    private BigDecimal unitprice;

    @JsonProperty("pictureurl")
    private String pictureurl;
}
