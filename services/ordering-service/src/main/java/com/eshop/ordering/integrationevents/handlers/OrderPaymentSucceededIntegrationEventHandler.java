package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.integrationevents.events.OrderPaymentSucceededIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStatusChangedToPaidIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link OrderPaymentSucceededIntegrationEvent} from the Payment service.
 *
 * <p>Transitions the order to PAID status and publishes the corresponding
 * integration event.
 */
@Component
public class OrderPaymentSucceededIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OrderPaymentSucceededIntegrationEventHandler.class);

    private final IOrderRepository orderRepository;
    private final IBuyerRepository buyerRepository;
    private final IEventBus eventBus;

    public OrderPaymentSucceededIntegrationEventHandler(
            IOrderRepository orderRepository,
            IBuyerRepository buyerRepository,
            IEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void handle(OrderPaymentSucceededIntegrationEvent event) throws Exception {
        log.info("Handling OrderPaymentSucceededIntegrationEvent for order #{}", event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order #{} not found", event.getOrderId());
            return;
        }

        order.setPaidStatus();
        orderRepository.update(order);

        String buyerName = resolveBuyerName(order);

        eventBus.publish(new OrderStatusChangedToPaidIntegrationEvent(
                order.getId(),
                buyerName,
                order.getOrderDate()));
    }

    private String resolveBuyerName(Order order) {
        if (order.getBuyerId() == null) {
            return "";
        }
        Buyer buyer = buyerRepository.findByIdentityGuid(order.getBuyerId().toString());
        return buyer != null ? buyer.getName() : "";
    }
}
