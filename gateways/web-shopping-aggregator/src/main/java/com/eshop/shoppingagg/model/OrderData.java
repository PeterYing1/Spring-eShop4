package com.eshop.shoppingagg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order draft / order detail DTO returned from the ordering service.
 *
 * <p>Property names intentionally match the lowercase JSON property names
 * produced by the ordering service's query layer (e.g. {@code ordernumber},
 * {@code orderitems}).
 */
@Data
@NoArgsConstructor
public class OrderData {

    @JsonProperty("ordernumber")
    private int ordernumber;

    @JsonProperty("date")
    private Instant date;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("orderitems")
    private List<OrderDataItem> orderItems;

    @JsonProperty("total")
    private BigDecimal total;
}
