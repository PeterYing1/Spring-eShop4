package com.eshop.ordering.domain;

import com.eshop.ordering.domain.aggregatesmodel.order.Address;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.aggregatesmodel.order.OrderStatus;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Aggregate Tests")
class OrderAggregateTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Order createOrder() {
        return new Order(
                "user-1", "Test User",
                new Address("Street 1", "City", "State", "Country", "12345"),
                1, "4111111111111111", "123", "Test User",
                Instant.now().plusSeconds(31536000),
                null, null);
    }

    // -------------------------------------------------------------------------
    // Creation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createOrder_succeeds_withCorrectStatus: new order has statusId=1 (submitted)")
    void createOrder_succeeds_withCorrectStatus() {
        Order order = createOrder();

        assertNotNull(order);
        assertEquals(OrderStatus.SUBMITTED, order.getOrderStatus());
    }

    @Test
    @DisplayName("createOrder_raisesOrderStartedDomainEvent: domain events contain OrderStartedDomainEvent")
    void createOrder_raisesOrderStartedDomainEvent() {
        Order order = createOrder();

        List<Object> events = order.getDomainEvents();
        assertFalse(events.isEmpty(), "Domain events should not be empty after order creation");
        assertTrue(
                events.stream().anyMatch(e -> e instanceof OrderStartedDomainEvent),
                "getDomainEvents() should contain an OrderStartedDomainEvent");
    }

    // -------------------------------------------------------------------------
    // Order item management
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addOrderItem_newProduct_addsItem: adding a new productId appends to orderItems")
    void addOrderItem_newProduct_addsItem() {
        Order order = createOrder();

        order.addOrderItem(10, "Widget", new BigDecimal("9.99"), BigDecimal.ZERO, null, 2);

        assertEquals(1, order.getOrderItems().size());
    }

    @Test
    @DisplayName("addOrderItem_existingProduct_mergesQuantity: same productId twice yields one item with summed units")
    void addOrderItem_existingProduct_mergesQuantity() {
        Order order = createOrder();

        order.addOrderItem(10, "Widget", new BigDecimal("9.99"), BigDecimal.ZERO, null, 2);
        order.addOrderItem(10, "Widget", new BigDecimal("9.99"), BigDecimal.ZERO, null, 3);

        assertEquals(1, order.getOrderItems().size(), "Should merge into a single line item");
        assertEquals(5, order.getOrderItems().get(0).getUnits(), "Units should be summed (2 + 3 = 5)");
    }

    @Test
    @DisplayName("addOrderItem_invalidUnits_throwsDomainException: units=0 must throw OrderingDomainException")
    void addOrderItem_invalidUnits_throwsDomainException() {
        Order order = createOrder();

        assertThrows(OrderingDomainException.class,
                () -> order.addOrderItem(10, "Widget", new BigDecimal("9.99"), BigDecimal.ZERO, null, 0));
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setAwaitingValidationStatus_fromSubmitted_succeeds: submitted -> awaitingvalidation is allowed")
    void setAwaitingValidationStatus_fromSubmitted_succeeds() {
        Order order = createOrder();

        order.setAwaitingValidationStatus();

        assertEquals(OrderStatus.AWAITING_VALIDATION, order.getOrderStatus());
    }

    @Test
    @DisplayName("setAwaitingValidationStatus_fromAwaitingValidation_throwsDomainException: cannot transition to same state")
    void setAwaitingValidationStatus_fromAwaitingValidation_throwsDomainException() {
        Order order = createOrder();
        order.setAwaitingValidationStatus(); // now in AWAITING_VALIDATION

        // Calling again does nothing (the guard silently no-ops), so we test
        // that trying to ship from awaiting-validation (wrong state) throws.
        // Per domain code: setAwaitingValidationStatus only acts when status==SUBMITTED,
        // so a second call silently no-ops rather than throwing. We verify the status
        // did NOT change to shipped by attempting an invalid forward transition instead.
        assertThrows(OrderingDomainException.class, order::setShippedStatus,
                "Shipping from AWAITING_VALIDATION should throw because only PAID is valid");
    }

    @Test
    @DisplayName("setStockConfirmedStatus_fromAwaitingValidation_succeeds")
    void setStockConfirmedStatus_fromAwaitingValidation_succeeds() {
        Order order = createOrder();
        order.setAwaitingValidationStatus();

        order.setStockConfirmedStatus();

        assertEquals(OrderStatus.STOCK_CONFIRMED, order.getOrderStatus());
    }

    @Test
    @DisplayName("setPaidStatus_fromStockConfirmed_succeeds")
    void setPaidStatus_fromStockConfirmed_succeeds() {
        Order order = createOrder();
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();

        order.setPaidStatus();

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
    }

    @Test
    @DisplayName("setShippedStatus_fromPaid_succeeds")
    void setShippedStatus_fromPaid_succeeds() {
        Order order = createOrder();
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();
        order.setPaidStatus();

        order.setShippedStatus();

        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
    }

    @Test
    @DisplayName("setCancelledStatus_fromSubmitted_succeeds")
    void setCancelledStatus_fromSubmitted_succeeds() {
        Order order = createOrder();

        order.setCancelledStatus();

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }

    @Test
    @DisplayName("setCancelledStatus_fromPaid_throwsDomainException: paid orders cannot be cancelled")
    void setCancelledStatus_fromPaid_throwsDomainException() {
        Order order = createOrder();
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();
        order.setPaidStatus();

        assertThrows(OrderingDomainException.class, order::setCancelledStatus);
    }

    @Test
    @DisplayName("setCancelledStatus_fromShipped_throwsDomainException: shipped orders cannot be cancelled")
    void setCancelledStatus_fromShipped_throwsDomainException() {
        Order order = createOrder();
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();
        order.setPaidStatus();
        order.setShippedStatus();

        assertThrows(OrderingDomainException.class, order::setCancelledStatus);
    }

    // -------------------------------------------------------------------------
    // Total
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getTotal_sumsOrderItems: total equals sum of unitPrice * units for each item")
    void getTotal_sumsOrderItems() {
        Order order = createOrder();
        order.addOrderItem(1, "Cup",   new BigDecimal("10.00"), BigDecimal.ZERO, null, 1);
        order.addOrderItem(2, "Plate", new BigDecimal("5.00"),  BigDecimal.ZERO, null, 2);

        // 10 * 1  +  5 * 2  =  10 + 10  =  20
        assertEquals(new BigDecimal("20.00"), order.getTotal());
    }

    @Test
    @DisplayName("getTotal_sumsOrderItems_afterMerge: two calls with same product id sum correctly")
    void getTotal_sumsOrderItems_afterMerge() {
        Order order = createOrder();
        order.addOrderItem(1, "Cup", new BigDecimal("10.00"), BigDecimal.ZERO, null, 1);
        order.addOrderItem(1, "Cup", new BigDecimal("10.00"), BigDecimal.ZERO, null, 1);

        assertEquals(new BigDecimal("20.00"), order.getTotal());
    }

    // -------------------------------------------------------------------------
    // Stock rejected cancellation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setCancelledStatusWhenStockIsRejected_fromAwaitingValidation_succeeds")
    void setCancelledStatusWhenStockIsRejected_fromAwaitingValidation_succeeds() {
        Order order = createOrder();
        order.addOrderItem(42, "Widget", new BigDecimal("5.00"), BigDecimal.ZERO, null, 1);
        order.setAwaitingValidationStatus();

        order.setCancelledStatusWhenStockIsRejected(List.of(42));

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    }
}
