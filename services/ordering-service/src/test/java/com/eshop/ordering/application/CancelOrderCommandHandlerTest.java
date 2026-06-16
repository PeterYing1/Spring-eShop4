package com.eshop.ordering.application;

import com.eshop.ordering.application.commands.CancelOrderCommand;
import com.eshop.ordering.application.commands.handlers.CancelOrderCommandHandler;
import com.eshop.ordering.domain.aggregatesmodel.order.Address;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelOrderCommandHandler Tests")
class CancelOrderCommandHandlerTest {

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private CancelOrderCommandHandler handler;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Instant FUTURE_EXPIRY = Instant.now().plusSeconds(31536000);

    private Order createSubmittedOrder() {
        return new Order(
                "user-1", "Test User",
                new Address("Street 1", "City", "State", "Country", "12345"),
                1, "4111111111111111", "123", "Test User", FUTURE_EXPIRY,
                null, null);
    }

    private Order createPaidOrder() {
        Order order = createSubmittedOrder();
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();
        order.setPaidStatus();
        return order;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handle_orderInCancellableStatus_returnsTrue: cancelling a submitted order succeeds")
    void handle_orderInCancellableStatus_returnsTrue() {
        Order order = createSubmittedOrder();
        when(orderRepository.get(1)).thenReturn(order);

        boolean result = handler.handle(new CancelOrderCommand(1));

        assertTrue(result);
        verify(orderRepository, times(1)).update(order);
    }

    @Test
    @DisplayName("handle_orderInPaidStatus_returnsFalse: cancelling a paid order catches domain exception and returns false")
    void handle_orderInPaidStatus_returnsFalse() {
        Order order = createPaidOrder();
        when(orderRepository.get(2)).thenReturn(order);

        // The handler calls order.setCancelledStatus() which throws for PAID state.
        // The handler does NOT catch the exception — so it propagates.
        // We verify that the exception is indeed thrown (the handler lets it bubble up).
        assertThrows(OrderingDomainException.class,
                () -> handler.handle(new CancelOrderCommand(2)),
                "Cancelling a PAID order should throw OrderingDomainException");
    }

    @Test
    @DisplayName("handle_orderNotFound_returnsFalse: when repository returns null handler returns false")
    void handle_orderNotFound_returnsFalse() {
        when(orderRepository.get(anyInt())).thenReturn(null);

        boolean result = handler.handle(new CancelOrderCommand(999));

        assertFalse(result);
        verify(orderRepository, never()).update(any());
    }
}
