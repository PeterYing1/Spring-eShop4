package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregatesmodel.order.Order;

import java.time.Instant;

/**
 * Raised when an {@link Order} is first created (submitted).
 *
 * <p>Handled by {@code ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler}
 * which locates or creates the buyer and verifies/adds the payment method.
 */
public class OrderStartedDomainEvent {

    private final Order order;
    private final String userId;
    private final String userName;
    private final int cardTypeId;
    private final String cardNumber;
    private final String cardSecurityNumber;
    private final String cardHolderName;
    private final Instant cardExpiration;

    public OrderStartedDomainEvent(Order order, String userId, String userName,
                                   int cardTypeId, String cardNumber,
                                   String cardSecurityNumber, String cardHolderName,
                                   Instant cardExpiration) {
        this.order = order;
        this.userId = userId;
        this.userName = userName;
        this.cardTypeId = cardTypeId;
        this.cardNumber = cardNumber;
        this.cardSecurityNumber = cardSecurityNumber;
        this.cardHolderName = cardHolderName;
        this.cardExpiration = cardExpiration;
    }

    public Order getOrder() { return order; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getCardTypeId() { return cardTypeId; }
    public String getCardNumber() { return cardNumber; }
    public String getCardSecurityNumber() { return cardSecurityNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public Instant getCardExpiration() { return cardExpiration; }
}
