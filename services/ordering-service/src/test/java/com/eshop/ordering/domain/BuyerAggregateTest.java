package com.eshop.ordering.domain;

import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.PaymentMethod;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Buyer Aggregate Tests")
class BuyerAggregateTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Instant FUTURE_EXPIRY = Instant.now().plusSeconds(31536000); // +1 year

    private Buyer createBuyer() {
        return new Buyer("identity-guid-1", "Test Buyer");
    }

    // -------------------------------------------------------------------------
    // Buyer creation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createBuyer_withIdentityAndName_succeeds: buyer is not null and fields are set")
    void createBuyer_withIdentityAndName_succeeds() {
        Buyer buyer = createBuyer();

        assertNotNull(buyer);
        assertEquals("identity-guid-1", buyer.getIdentityGuid());
        assertEquals("Test Buyer", buyer.getName());
    }

    @Test
    @DisplayName("createBuyer_withBlankIdentity_throwsDomainException: blank identityGuid is rejected")
    void createBuyer_withBlankIdentity_throwsDomainException() {
        assertThrows(OrderingDomainException.class,
                () -> new Buyer("", "Test Buyer"));
    }

    @Test
    @DisplayName("createBuyer_withNullIdentity_throwsDomainException: null identityGuid is rejected")
    void createBuyer_withNullIdentity_throwsDomainException() {
        assertThrows(OrderingDomainException.class,
                () -> new Buyer(null, "Test Buyer"));
    }

    // -------------------------------------------------------------------------
    // Payment method management
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("verifyOrAddPaymentMethod_newMethod_addsToList: first call adds payment method to the list")
    void verifyOrAddPaymentMethod_newMethod_addsToList() {
        Buyer buyer = createBuyer();

        PaymentMethod result = buyer.verifyOrAddPaymentMethod(
                1, "My Visa", "4111111111111111", "123", "Test Buyer", FUTURE_EXPIRY, 1);

        assertNotNull(result);
        assertEquals(1, buyer.getPaymentMethods().size());
    }

    @Test
    @DisplayName("verifyOrAddPaymentMethod_existingMethodSameDetails_reusesExisting: same card details do not add a second entry")
    void verifyOrAddPaymentMethod_existingMethodSameDetails_reusesExisting() {
        Buyer buyer = createBuyer();
        Instant expiry = FUTURE_EXPIRY;

        buyer.verifyOrAddPaymentMethod(1, "My Visa", "4111111111111111", "123", "Test Buyer", expiry, 1);
        // Second call with identical cardTypeId, cardNumber, expiration — should reuse
        PaymentMethod result = buyer.verifyOrAddPaymentMethod(
                1, "My Visa", "4111111111111111", "123", "Test Buyer", expiry, 2);

        assertNotNull(result);
        assertEquals(1, buyer.getPaymentMethods().size(),
                "Same card details should not add a new payment method entry");
    }

    @Test
    @DisplayName("verifyOrAddPaymentMethod_differentCard_addsNew: different card number results in a second entry")
    void verifyOrAddPaymentMethod_differentCard_addsNew() {
        Buyer buyer = createBuyer();

        buyer.verifyOrAddPaymentMethod(1, "Card A", "4111111111111111", "123", "Test Buyer", FUTURE_EXPIRY, 1);
        buyer.verifyOrAddPaymentMethod(1, "Card B", "5500005555555559", "456", "Test Buyer", FUTURE_EXPIRY, 2);

        assertEquals(2, buyer.getPaymentMethods().size(),
                "Different card number should be added as a new payment method");
    }

    @Test
    @DisplayName("verifyOrAddPaymentMethod_raisesEvent: calling verifyOrAddPaymentMethod raises BuyerAndPaymentMethodVerifiedDomainEvent")
    void verifyOrAddPaymentMethod_raisesEvent() {
        Buyer buyer = createBuyer();

        buyer.verifyOrAddPaymentMethod(1, "My Visa", "4111111111111111", "123", "Test Buyer", FUTURE_EXPIRY, 1);

        assertFalse(buyer.getDomainEvents().isEmpty(),
                "Verifying/adding a payment method should raise a domain event");
    }
}
