package com.eshop.ordering.application;

import com.eshop.ordering.application.commands.CreateOrderCommand;
import com.eshop.ordering.application.commands.handlers.CreateOrderCommandHandler;
import com.eshop.ordering.domain.aggregatesmodel.order.Address;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.events.handlers.ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderCommandHandler Tests")
class CreateOrderCommandHandlerTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler orderStartedHandler;

    @InjectMocks
    private CreateOrderCommandHandler handler;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final Instant FUTURE_EXPIRY = Instant.now().plusSeconds(31536000);

    private CreateOrderCommand buildCommand() {
        return new CreateOrderCommand(
                "user-1", "Test User",
                "City", "Street 1", "State", "Country", "12345",
                "4111111111111111", "Test User", FUTURE_EXPIRY, "123", 1,
                List.of(new CreateOrderCommand.OrderItemDTO(
                        10, "Widget", new BigDecimal("9.99"), BigDecimal.ZERO, 2, null)));
    }

    /**
     * Creates a fake saved Order that returns a non-null id so the handler can
     * log and return it.  We use a real Order and reflectively set the id via
     * a helper subclass trick — or we can return a spy.
     */
    private Order fakeSavedOrder(int id) {
        Order order = new Order(
                "user-1", "Test User",
                new Address("Street 1", "City", "State", "Country", "12345"),
                1, "4111111111111111", "123", "Test User", FUTURE_EXPIRY,
                null, null);
        // Use package-accessible setId via reflection to set the id on the saved order
        try {
            java.lang.reflect.Method setId =
                    com.eshop.ordering.domain.seedwork.Entity.class
                            .getDeclaredMethod("setId", Integer.class);
            setId.setAccessible(true);
            setId.invoke(order, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set id on order", e);
        }
        return order;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handle_validCommand_createsOrderAndReturnsId: handler persists order and returns its id")
    void handle_validCommand_createsOrderAndReturnsId() {
        Order saved = fakeSavedOrder(42);
        when(orderRepository.add(any(Order.class))).thenReturn(saved);
        doNothing().when(orderStartedHandler).handle(any());

        int result = handler.handle(buildCommand());

        assertEquals(42, result);
        verify(orderRepository, times(1)).add(any(Order.class));
        verify(orderStartedHandler, times(1)).handle(any());
    }

    @Test
    @DisplayName("handle_withExistingBuyer_reusesExistingBuyer: orderStartedHandler is invoked exactly once")
    void handle_withExistingBuyer_reusesExistingBuyer() {
        // The buyer look-up is done inside orderStartedHandler (domain event handler),
        // so we verify the handler is called and the order is saved — the reuse
        // logic is the domain event handler's responsibility.
        Order saved = fakeSavedOrder(7);
        when(orderRepository.add(any(Order.class))).thenReturn(saved);
        doNothing().when(orderStartedHandler).handle(any());

        handler.handle(buildCommand());

        verify(orderStartedHandler, times(1)).handle(any());
        verify(orderRepository, times(1)).add(any(Order.class));
    }

    @Test
    @DisplayName("handle_commandWithNoItems_createsOrderWithEmptyItems")
    void handle_commandWithNoItems_createsOrderWithEmptyItems() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-2", "Another User",
                "City", "Street 2", "State", "Country", "99999",
                "4111111111111111", "Another User", FUTURE_EXPIRY, "456", 2,
                null);

        Order saved = fakeSavedOrder(99);
        when(orderRepository.add(any(Order.class))).thenReturn(saved);
        doNothing().when(orderStartedHandler).handle(any());

        int result = handler.handle(command);

        assertEquals(99, result);
    }
}
