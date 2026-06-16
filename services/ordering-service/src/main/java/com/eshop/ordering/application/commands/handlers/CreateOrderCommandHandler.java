package com.eshop.ordering.application.commands.handlers;

import com.eshop.ordering.application.commands.CreateOrderCommand;
import com.eshop.ordering.domain.aggregatesmodel.order.Address;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import com.eshop.ordering.domain.events.handlers.ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link CreateOrderCommand}.
 *
 * <p>Creates the {@link Order} aggregate, persists it, then synchronously
 * dispatches the {@link OrderStartedDomainEvent} to
 * {@link ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler}.
 */
@Service
public class CreateOrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderCommandHandler.class);

    private final IOrderRepository orderRepository;
    private final ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler orderStartedHandler;

    public CreateOrderCommandHandler(
            IOrderRepository orderRepository,
            ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler orderStartedHandler) {
        this.orderRepository = orderRepository;
        this.orderStartedHandler = orderStartedHandler;
    }

    /**
     * Creates and persists a new order from the command.
     *
     * @param command the create order command
     * @return the id of the newly created order
     */
    @Transactional
    public int handle(CreateOrderCommand command) {
        log.info("----- Creating order for user '{}'", command.getUserId());

        Address address = new Address(
                command.getStreet(),
                command.getCity(),
                command.getState(),
                command.getCountry(),
                command.getZipCode());

        Order order = new Order(
                command.getUserId(),
                command.getUserName(),
                address,
                command.getCardTypeId(),
                command.getCardNumber(),
                command.getCardSecurityNumber(),
                command.getCardHolderName(),
                command.getCardExpiration(),
                null,
                null);

        if (command.getOrderItems() != null) {
            for (CreateOrderCommand.OrderItemDTO item : command.getOrderItems()) {
                order.addOrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getDiscount() != null ? item.getDiscount() : java.math.BigDecimal.ZERO,
                        item.getPictureUrl(),
                        item.getUnits());
            }
        }

        Order saved = orderRepository.add(order);

        // Dispatch domain events synchronously
        for (Object event : saved.getDomainEvents()) {
            if (event instanceof OrderStartedDomainEvent) {
                orderStartedHandler.handle((OrderStartedDomainEvent) event);
            }
        }
        saved.clearDomainEvents();

        log.info("Order #{} created for user '{}'", saved.getId(), command.getUserId());
        return saved.getId();
    }
}
