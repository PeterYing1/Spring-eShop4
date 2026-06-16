package com.eshop.basket.integrationevents.events;

import com.eshop.basket.domain.CustomerBasket;
import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when the user initiates checkout. Consumed by the Ordering service
 * to create an order from the basket contents.
 *
 * <p>All field names use {@code @JsonProperty} with PascalCase names to match
 * the .NET Newtonsoft.Json serialisation convention expected by the Ordering
 * service consumer.
 */
public class UserCheckoutAcceptedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("UserId")
    private final String userId;

    @JsonProperty("UserName")
    private final String userName;

    @JsonProperty("City")
    private final String city;

    @JsonProperty("Street")
    private final String street;

    @JsonProperty("State")
    private final String state;

    @JsonProperty("Country")
    private final String country;

    @JsonProperty("ZipCode")
    private final String zipCode;

    @JsonProperty("CardNumber")
    private final String cardNumber;

    @JsonProperty("CardHolderName")
    private final String cardHolderName;

    @JsonProperty("CardExpiration")
    private final Instant cardExpiration;

    @JsonProperty("CardSecurityNumber")
    private final String cardSecurityNumber;

    @JsonProperty("CardTypeId")
    private final int cardTypeId;

    @JsonProperty("Buyer")
    private final String buyer;

    @JsonProperty("RequestId")
    private final UUID requestId;

    @JsonProperty("Basket")
    private final CustomerBasket basket;

    public UserCheckoutAcceptedIntegrationEvent(
            String userId,
            String userName,
            String city,
            String street,
            String state,
            String country,
            String zipCode,
            String cardNumber,
            String cardHolderName,
            Instant cardExpiration,
            String cardSecurityNumber,
            int cardTypeId,
            String buyer,
            UUID requestId,
            CustomerBasket basket) {
        super();
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
        this.buyer = buyer;
        this.requestId = requestId;
        this.basket = basket;
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
    public CustomerBasket getBasket() { return basket; }
}
