package com.eshop.ordering.application.queries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * View model returned by {@code GET /api/v1/orders/{orderId}}.
 *
 * <p>All JSON property names are lowercase to match the .NET contract used
 * by the Web MVC and Angular SPA clients.
 */
public class OrderViewModel {

    @JsonProperty("ordernumber")
    private int ordernumber;

    @JsonProperty("date")
    private Instant date;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("street")
    private String street;

    @JsonProperty("city")
    private String city;

    @JsonProperty("zipcode")
    private String zipcode;

    @JsonProperty("country")
    private String country;

    @JsonProperty("orderitems")
    private List<OrderItemSummary> orderitems;

    @JsonProperty("total")
    private BigDecimal total;

    public OrderViewModel() {
    }

    public int getOrdernumber() { return ordernumber; }
    public void setOrdernumber(int ordernumber) { this.ordernumber = ordernumber; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public List<OrderItemSummary> getOrderitems() { return orderitems; }
    public void setOrderitems(List<OrderItemSummary> orderitems) { this.orderitems = orderitems; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * Summary of a single order line item within an order view.
     */
    public static class OrderItemSummary {

        @JsonProperty("productname")
        private String productname;

        @JsonProperty("units")
        private int units;

        @JsonProperty("unitprice")
        private BigDecimal unitprice;

        @JsonProperty("pictureurl")
        private String pictureurl;

        public OrderItemSummary() {
        }

        public String getProductname() { return productname; }
        public void setProductname(String productname) { this.productname = productname; }

        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }

        public BigDecimal getUnitprice() { return unitprice; }
        public void setUnitprice(BigDecimal unitprice) { this.unitprice = unitprice; }

        public String getPictureurl() { return pictureurl; }
        public void setPictureurl(String pictureurl) { this.pictureurl = pictureurl; }
    }
}
