package com.eshop.ordering.application.commands.handlers;

import com.eshop.ordering.application.commands.CancelOrderCommand;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link CancelOrderCommand}.
 *
 * <p>Loads the order, transitions it to cancelled status, and saves.
 */
@Service
public class CancelOrderCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderCommandHandler.class);

    private final IOrderRepository orderRepository;

    public CancelOrderCommandHandler(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Cancels the order identified by {@code command.getOrderNumber()}.
     *
     * @param command the cancel command
     * @return {@code true} if the order was found and cancelled; {@code false} otherwise
     */
    @Transactional
    public boolean handle(CancelOrderCommand command) {
        log.info("----- Cancelling order #{}", command.getOrderNumber());

        Order order = orderRepository.get(command.getOrderNumber());
        if (order == null) {
            log.warn("Order #{} not found", command.getOrderNumber());
            return false;
        }

        order.setCancelledStatus();
        orderRepository.update(order);
        return true;
    }
}
