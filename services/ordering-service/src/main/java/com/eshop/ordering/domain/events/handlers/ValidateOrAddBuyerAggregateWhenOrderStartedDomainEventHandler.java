package com.eshop.ordering.domain.events.handlers;

import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link OrderStartedDomainEvent}.
 *
 * <p>Finds or creates the buyer for the authenticated user, then calls
 * {@link Buyer#verifyOrAddPaymentMethod} to ensure a matching payment method
 * exists.  Updates the order with the resolved buyer and payment method ids.
 */
@Service
public class ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler.class);

    private final IBuyerRepository buyerRepository;
    private final IOrderRepository orderRepository;

    public ValidateOrAddBuyerAggregateWhenOrderStartedDomainEventHandler(
            IBuyerRepository buyerRepository,
            IOrderRepository orderRepository) {
        this.buyerRepository = buyerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void handle(OrderStartedDomainEvent event) {
        log.info("Handling OrderStartedDomainEvent for user '{}' (order id={})",
                event.getUserId(), event.getOrder().getId());

        Buyer buyer = buyerRepository.findByIdentityGuid(event.getUserId());
        boolean buyerOriginallyExisted = buyer != null;

        if (!buyerOriginallyExisted) {
            buyer = new Buyer(event.getUserId(), event.getUserName());
        }

        var payment = buyer.verifyOrAddPaymentMethod(
                event.getCardTypeId(),
                String.format("Payment method on %s", event.getCardHolderName()),
                event.getCardNumber(),
                event.getCardSecurityNumber(),
                event.getCardHolderName(),
                event.getCardExpiration(),
                event.getOrder().getId() != null ? event.getOrder().getId() : 0);

        Buyer savedBuyer = buyerOriginallyExisted
                ? buyerRepository.update(buyer)
                : buyerRepository.add(buyer);

        log.info("Buyer {} {}  (id={}) for order {}",
                savedBuyer.getName(),
                buyerOriginallyExisted ? "updated" : "created",
                savedBuyer.getId(),
                event.getOrder().getId());

        // Update the order with resolved buyer and payment method ids
        event.getOrder().setBuyerId(savedBuyer.getId());
        if (payment.getId() != null) {
            event.getOrder().setPaymentId(payment.getId());
        }
        orderRepository.update(event.getOrder());
    }
}
