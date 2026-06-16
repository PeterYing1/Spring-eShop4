package com.eshop.ordering.application;

import com.eshop.ordering.application.commands.ShipOrderCommand;
import com.eshop.ordering.application.commands.handlers.ShipOrderCommandHandler;
import com.eshop.ordering.domain.aggregatesmodel.order.Address;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
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
@DisplayName("ShipOrderCommandHandler Tests")
class ShipOrderCommandHandlerTest {

    @Mock
    private IOrderRepository orderRepository;

    @InjectMocks
    private ShipOrderCommandHandler handler;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Instant FUTURE_EXPIRY = Instant.now().plusSeconds(31536000);

    private Order createPaidOrder() {
        Order order = new Order(
                "user-1", "Test User",
                new Address("Street 1", "City", "State", "Country", "12345"),
                1, "4111111111111111", "123", "Test User", FUTURE_EXPIRY,
                null, null);
        order.setAwaitingValidationStatus();
        order.setStockConfirmedStatus();
        order.setPaidStatus();
        return order;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handle_orderInPaidStatus_returnsTrue: shipping a paid order succeeds")
    void handle_orderInPaidStatus_returnsTrue() {
        Order order = createPaidOrder();
        when(orderRepository.get(1)).thenReturn(order);

        boolean result = handler.handle(new ShipOrderCommand(1));

        assertTrue(result);
        verify(orderRepository, times(1)).update(order);
    }

    @Test
    @DisplayName("handle_orderNotFound_returnsFalse: when repository returns null handler returns false")
    void handle_orderNotFound_returnsFalse() {
        when(orderRepository.get(anyInt())).thenReturn(null);

        boolean result = handler.handle(new ShipOrderCommand(999));

        assertFalse(result);
        verify(orderRepository, never()).update(any());
    }
}
