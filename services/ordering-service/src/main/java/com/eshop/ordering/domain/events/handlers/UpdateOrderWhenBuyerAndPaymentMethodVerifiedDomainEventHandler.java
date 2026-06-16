package com.eshop.ordering.domain.events.handlers;

import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.events.BuyerAndPaymentMethodVerifiedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link BuyerAndPaymentMethodVerifiedDomainEvent}.
 *
 * <p>Sets the buyer id and payment method id on the order.
 */
@Service
public class UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler.class);

    private final IOrderRepository orderRepository;

    public UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void handle(BuyerAndPaymentMethodVerifiedDomainEvent event) {
        log.info("Handling BuyerAndPaymentMethodVerifiedDomainEvent for order id={}",
                event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order {} not found when processing BuyerAndPaymentMethodVerifiedDomainEvent",
                    event.getOrderId());
            return;
        }

        order.setBuyerId(event.getBuyer().getId());
        if (event.getPayment().getId() != null) {
            order.setPaymentId(event.getPayment().getId());
        }
        orderRepository.update(order);

        log.info("Order {} updated with buyerId={} paymentMethodId={}",
                event.getOrderId(),
                event.getBuyer().getId(),
                event.getPayment().getId());
    }
}
