package com.eshop.ordering.application.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Command to create a new order from a checkout event.
 *
 * <p>Populated by {@code UserCheckoutAcceptedIntegrationEventHandler} from the
 * incoming {@code UserCheckoutAcceptedIntegrationEvent}.
 */
public class CreateOrderCommand {

    private String userId;
    private String userName;
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private String cardNumber;
    private String cardHolderName;
    private Instant cardExpiration;
    private String cardSecurityNumber;
    private int cardTypeId;
    private List<OrderItemDTO> orderItems;

    public CreateOrderCommand() {
    }

    public CreateOrderCommand(String userId, String userName, String city, String street,
                              String state, String country, String zipCode,
                              String cardNumber, String cardHolderName, Instant cardExpiration,
                              String cardSecurityNumber, int cardTypeId,
                              List<OrderItemDTO> orderItems) {
        this.userId = userId;
        this.userName = userName;
        this.city = city;
        this.street = street;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiration = cardExpiration;
        this.cardSecurityNumber = cardSecurityNumber;
        this.cardTypeId = cardTypeId;
        this.orderItems = orderItems;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public Instant getCardExpiration() { return cardExpiration; }
    public void setCardExpiration(Instant cardExpiration) { this.cardExpiration = cardExpiration; }

    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public void setCardSecurityNumber(String cardSecurityNumber) { this.cardSecurityNumber = cardSecurityNumber; }

    public int getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(int cardTypeId) { this.cardTypeId = cardTypeId; }

    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }

    // -------------------------------------------------------------------------
    // Nested DTO
    // -------------------------------------------------------------------------

    /**
     * A single basket / order line item carried inside {@link CreateOrderCommand}.
     */
    public static class OrderItemDTO {

        @JsonProperty("productId")
        private int productId;

        @JsonProperty("productName")
        private String productName;

        @JsonProperty("unitPrice")
        private BigDecimal unitPrice;

        @JsonProperty("discount")
        private BigDecimal discount;

        @JsonProperty("units")
        private int units;

        @JsonProperty("pictureUrl")
        private String pictureUrl;

        public OrderItemDTO() {
        }

        public OrderItemDTO(int productId, String productName, BigDecimal unitPrice,
                            BigDecimal discount, int units, String pictureUrl) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.discount = discount;
            this.units = units;
            this.pictureUrl = pictureUrl;
        }

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }

        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }

        public String getPictureUrl() { return pictureUrl; }
        public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    }
}
