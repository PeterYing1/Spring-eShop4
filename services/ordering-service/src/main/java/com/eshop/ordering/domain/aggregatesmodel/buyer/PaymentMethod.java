package com.eshop.ordering.domain.aggregatesmodel.buyer;

import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Payment method child entity of the {@link Buyer} aggregate.
 *
 * <p>Validates that required card fields are non-blank and that the card has
 * not already expired at construction time.
 */
@jakarta.persistence.Entity
@Table(name = "paymentmethods", schema = "ordering")
public class PaymentMethod extends Entity {

    @Column(name = "Alias")
    private String alias;

    @Column(name = "BuyerId", nullable = false)
    private int buyerId;

    @Column(name = "CardHolderName", nullable = false)
    private String cardHolderName;

    @Column(name = "CardNumber", nullable = false, length = 25)
    private String cardNumber;

    @Column(name = "CardTypeId", nullable = false)
    private int cardTypeId;

    @Column(name = "Expiration", nullable = false)
    private Instant expiration;

    /** Required by JPA. */
    protected PaymentMethod() {
    }

    /**
     * Creates a new payment method with full validation.
     *
     * @param cardTypeId     id of the card type (FK to ordering.cardtypes)
     * @param alias          optional alias for the card
     * @param cardNumber     card number; must not be blank
     * @param securityNumber CVV; must not be blank
     * @param cardHolderName name on the card; must not be blank
     * @param expiration     card expiry; must be in the future
     * @throws OrderingDomainException if any validation fails
     */
    public PaymentMethod(int cardTypeId, String alias, String cardNumber,
                         String securityNumber, String cardHolderName, Instant expiration) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new OrderingDomainException("cardNumber");
        }
        if (securityNumber == null || securityNumber.isBlank()) {
            throw new OrderingDomainException("securityNumber");
        }
        if (cardHolderName == null || cardHolderName.isBlank()) {
            throw new OrderingDomainException("cardHolderName");
        }
        if (expiration.isBefore(Instant.now())) {
            throw new OrderingDomainException("expiration");
        }

        this.cardTypeId = cardTypeId;
        this.alias = alias;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiration = expiration;
    }

    /**
     * Returns {@code true} when the card type, number, and expiration all match.
     * Used by {@link Buyer#verifyOrAddPaymentMethod} to detect duplicates.
     */
    public boolean isEqualTo(int cardTypeId, String cardNumber, Instant expiration) {
        return this.cardTypeId == cardTypeId
                && this.cardNumber.equals(cardNumber)
                && this.expiration.equals(expiration);
    }

    public String getAlias() { return alias; }
    public int getBuyerId() { return buyerId; }
    public String getCardHolderName() { return cardHolderName; }
    public String getCardNumber() { return cardNumber; }
    public int getCardTypeId() { return cardTypeId; }
    public Instant getExpiration() { return expiration; }
}
