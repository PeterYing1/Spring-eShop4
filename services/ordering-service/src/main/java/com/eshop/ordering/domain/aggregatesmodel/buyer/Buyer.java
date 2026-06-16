package com.eshop.ordering.domain.aggregatesmodel.buyer;

import com.eshop.ordering.domain.events.BuyerAndPaymentMethodVerifiedDomainEvent;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import com.eshop.ordering.domain.seedwork.Entity;
import com.eshop.ordering.domain.seedwork.IAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Buyer aggregate root.
 *
 * <p>A buyer represents a registered user who has placed at least one order.
 * The aggregate manages the collection of payment methods for that buyer.
 */
@jakarta.persistence.Entity
@Table(name = "buyers", schema = "ordering")
public class Buyer extends Entity implements IAggregateRoot {

    @Column(name = "IdentityGuid", nullable = false, unique = true)
    private String identityGuid;

    @Column(name = "Name", nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "BuyerId")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    /** Required by JPA. */
    protected Buyer() {
    }

    /**
     * Creates a new buyer.
     *
     * @param identityGuid the OAuth subject (must not be blank)
     * @param name         display name (must not be blank)
     * @throws OrderingDomainException if either argument is blank
     */
    public Buyer(String identityGuid, String name) {
        if (identityGuid == null || identityGuid.isBlank()) {
            throw new OrderingDomainException("identityGuid must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new OrderingDomainException("name must not be blank");
        }
        this.identityGuid = identityGuid;
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Finds an existing payment method that matches the given card details, or
     * creates a new one.  In both cases raises a
     * {@link BuyerAndPaymentMethodVerifiedDomainEvent}.
     *
     * @param cardTypeId      card type id
     * @param alias           optional alias
     * @param cardNumber      card number
     * @param securityNumber  CVV
     * @param cardHolderName  name on card
     * @param expiration      card expiry
     * @param orderId         the order for which the payment is being verified
     * @return the existing or newly created payment method
     */
    public PaymentMethod verifyOrAddPaymentMethod(
            int cardTypeId, String alias, String cardNumber,
            String securityNumber, String cardHolderName,
            Instant expiration, int orderId) {

        PaymentMethod existing = paymentMethods.stream()
                .filter(p -> p.isEqualTo(cardTypeId, cardNumber, expiration))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            addDomainEvent(new BuyerAndPaymentMethodVerifiedDomainEvent(this, existing, orderId));
            return existing;
        }

        PaymentMethod payment = new PaymentMethod(
                cardTypeId, alias, cardNumber, securityNumber, cardHolderName, expiration);
        paymentMethods.add(payment);
        addDomainEvent(new BuyerAndPaymentMethodVerifiedDomainEvent(this, payment, orderId));
        return payment;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getIdentityGuid() { return identityGuid; }
    public String getName() { return name; }
    public List<PaymentMethod> getPaymentMethods() { return Collections.unmodifiableList(paymentMethods); }
}
