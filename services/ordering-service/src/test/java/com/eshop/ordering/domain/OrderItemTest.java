package com.eshop.ordering.domain;

import com.eshop.ordering.domain.aggregatesmodel.order.OrderItem;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderItem Tests")
class OrderItemTest {

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createOrderItem_validParams_succeeds: valid args produce a non-null OrderItem")
    void createOrderItem_validParams_succeeds() {
        OrderItem item = new OrderItem(
                1, "Widget", new BigDecimal("12.00"), new BigDecimal("2.00"), "http://img", 5);

        assertNotNull(item);
        assertEquals(1, item.getProductId());
        assertEquals("Widget", item.getOrderItemProductName());
        assertEquals(new BigDecimal("12.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("2.00"), item.getCurrentDiscount());
        assertEquals(5, item.getUnits());
    }

    @Test
    @DisplayName("createOrderItem_zeroUnits_throwsDomainException: units=0 must be rejected")
    void createOrderItem_zeroUnits_throwsDomainException() {
        assertThrows(OrderingDomainException.class,
                () -> new OrderItem(1, "Widget", new BigDecimal("12.00"), BigDecimal.ZERO, null, 0));
    }

    @Test
    @DisplayName("createOrderItem_negativeUnits_throwsDomainException: units=-1 must be rejected")
    void createOrderItem_negativeUnits_throwsDomainException() {
        assertThrows(OrderingDomainException.class,
                () -> new OrderItem(1, "Widget", new BigDecimal("12.00"), BigDecimal.ZERO, null, -1));
    }

    @Test
    @DisplayName("createOrderItem_discountGreaterThanTotal_throwsDomainException: discount > unitPrice*units is invalid")
    void createOrderItem_discountGreaterThanTotal_throwsDomainException() {
        // unitPrice=12, units=1 => total=12; discount=15 > 12 => should throw
        assertThrows(OrderingDomainException.class,
                () -> new OrderItem(1, "Widget", new BigDecimal("12.00"), new BigDecimal("15.00"), null, 1));
    }

    // -------------------------------------------------------------------------
    // addUnits
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addUnits_addsToExistingUnits: addUnits increments units correctly")
    void addUnits_addsToExistingUnits() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), BigDecimal.ZERO, null, 3);

        item.addUnits(4);

        assertEquals(7, item.getUnits());
    }

    @Test
    @DisplayName("addUnits_negativeValue_throwsDomainException: negative units must be rejected")
    void addUnits_negativeValue_throwsDomainException() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), BigDecimal.ZERO, null, 5);

        assertThrows(OrderingDomainException.class, () -> item.addUnits(-1));
    }

    @Test
    @DisplayName("addUnits_zero_isAllowed: adding zero units is a no-op")
    void addUnits_zero_isAllowed() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), BigDecimal.ZERO, null, 5);

        item.addUnits(0);

        assertEquals(5, item.getUnits());
    }

    // -------------------------------------------------------------------------
    // setNewDiscount
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setNewDiscount_negativeValue_throwsDomainException: negative discount is invalid")
    void setNewDiscount_negativeValue_throwsDomainException() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), new BigDecimal("2.00"), null, 5);

        assertThrows(OrderingDomainException.class,
                () -> item.setNewDiscount(new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("setNewDiscount_positiveValue_updatesDiscount: valid new discount is applied")
    void setNewDiscount_positiveValue_updatesDiscount() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), new BigDecimal("2.00"), null, 5);

        item.setNewDiscount(new BigDecimal("3.00"));

        assertEquals(new BigDecimal("3.00"), item.getCurrentDiscount());
    }

    @Test
    @DisplayName("setNewDiscount_zero_isAllowed: discount of zero is valid")
    void setNewDiscount_zero_isAllowed() {
        OrderItem item = new OrderItem(1, "Widget", new BigDecimal("12.00"), new BigDecimal("2.00"), null, 5);

        item.setNewDiscount(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, item.getCurrentDiscount());
    }
}
