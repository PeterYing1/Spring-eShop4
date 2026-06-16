package com.eshop.ordering.application.queries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Summary row returned by {@code GET /api/v1/orders} (list for a user).
 *
 * <p>All JSON property names are lowercase to match the .NET contract.
 */
public class OrderSummary {

    @JsonProperty("ordernumber")
    private int ordernumber;

    @JsonProperty("date")
    private Instant date;

    @JsonProperty("status")
    private String status;

    @JsonProperty("total")
    private BigDecimal total;

    public OrderSummary() {
    }

    public int getOrdernumber() { return ordernumber; }
    public void setOrdernumber(int ordernumber) { this.ordernumber = ordernumber; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
