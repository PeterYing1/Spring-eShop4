package com.eshop.ordering.application.commands.handlers;

import com.eshop.ordering.application.commands.ShipOrderCommand;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link ShipOrderCommand}.
 *
 * <p>Loads the order, transitions it to shipped status, and saves.
 * Only orders in PAID state can be shipped; throws
 * {@link com.eshop.ordering.domain.exceptions.OrderingDomainException} otherwise.
 */
@Service
public class ShipOrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(ShipOrderCommandHandler.class);

    private final IOrderRepository orderRepository;

    public ShipOrderCommandHandler(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Ships the order identified by {@code command.getOrderNumber()}.
     *
     * @param command the ship command
     * @return {@code true} if the order was found and shipped; {@code false} otherwise
     */
    @Transactional
    public boolean handle(ShipOrderCommand command) {
        log.info("----- Shipping order #{}", command.getOrderNumber());

        Order order = orderRepository.get(command.getOrderNumber());
        if (order == null) {
            log.warn("Order #{} not found", command.getOrderNumber());
            return false;
        }

        order.setShippedStatus();
        orderRepository.update(order);
        return true;
    }
}
