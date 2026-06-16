package com.eshop.ordering.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Received from the Basket service when the user initiates checkout.
 *
 * <p>Handled by
 * {@link com.eshop.ordering.integrationevents.handlers.UserCheckoutAcceptedIntegrationEventHandler}
 * which creates a new order.
 */
public class UserCheckoutAcceptedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("UserName")
    private String userName;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Street")
    private String street;

    @JsonProperty("State")
    private String state;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("ZipCode")
    private String zipCode;

    @JsonProperty("CardNumber")
    private String cardNumber;

    @JsonProperty("CardHolderName")
    private String cardHolderName;

    @JsonProperty("CardExpiration")
    private Instant cardExpiration;

    @JsonProperty("CardSecurityNumber")
    private String cardSecurityNumber;

    @JsonProperty("CardTypeId")
    private int cardTypeId;

    @JsonProperty("Buyer")
    private String buyer;

    @JsonProperty("RequestId")
    private UUID requestId;

    @JsonProperty("Basket")
    private CustomerBasketDTO basket;

    public UserCheckoutAcceptedIntegrationEvent() {
        super();
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCity() { return city; }
    public String getStreet() { return street; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public int getCardTypeId() { return cardTypeId; }
    public String getBuyer() { return buyer; }
    public UUID getRequestId() { return requestId; }
    public CustomerBasketDTO getBasket() { return basket; }

    // -------------------------------------------------------------------------
    // Nested DTOs
    // -------------------------------------------------------------------------

    /**
     * A user's basket at the time of checkout.
     */
    public static class CustomerBasketDTO {

        @JsonProperty("BuyerId")
        private String buyerId;

        @JsonProperty("Items")
        private List<BasketItemDTO> items;

        public CustomerBasketDTO() {
        }

        public String getBuyerId() { return buyerId; }
        public List<BasketItemDTO> getItems() { return items; }
    }

    /**
     * A single basket item.
     */
    public static class BasketItemDTO {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("ProductId")
        private int productId;

        @JsonProperty("ProductName")
        private String productName;

        @JsonProperty("UnitPrice")
        private java.math.BigDecimal unitPrice;

        @JsonProperty("OldUnitPrice")
        private java.math.BigDecimal oldUnitPrice;

        @JsonProperty("Quantity")
        private int quantity;

        @JsonProperty("PictureUrl")
        private String pictureUrl;

        public BasketItemDTO() {
        }

        public String getId() { return id; }
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public java.math.BigDecimal getOldUnitPrice() { return oldUnitPrice; }
        public int getQuantity() { return quantity; }
        public String getPictureUrl() { return pictureUrl; }
    }
}
