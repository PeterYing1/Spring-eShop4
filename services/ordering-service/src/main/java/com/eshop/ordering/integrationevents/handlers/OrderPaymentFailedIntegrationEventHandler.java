package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.integrationevents.events.OrderPaymentFailedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStatusChangedToCancelledIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link OrderPaymentFailedIntegrationEvent} from the Payment service.
 *
 * <p>Cancels the order and publishes the cancelled integration event.
 */
@Component
public class OrderPaymentFailedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OrderPaymentFailedIntegrationEventHandler.class);

    private final IOrderRepository orderRepository;
    private final IBuyerRepository buyerRepository;
    private final IEventBus eventBus;

    public OrderPaymentFailedIntegrationEventHandler(
            IOrderRepository orderRepository,
            IBuyerRepository buyerRepository,
            IEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void handle(OrderPaymentFailedIntegrationEvent event) throws Exception {
        log.info("Handling OrderPaymentFailedIntegrationEvent for order #{}", event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order #{} not found", event.getOrderId());
            return;
        }

        order.setCancelledStatus();
        orderRepository.update(order);

        String buyerName = resolveBuyerName(order);

        eventBus.publish(new OrderStatusChangedToCancelledIntegrationEvent(
                order.getId(),
                order.getOrderStatus().getName(),
                buyerName));
    }

    private String resolveBuyerName(Order order) {
        if (order.getBuyerId() == null) {
            return "";
        }
        Buyer buyer = buyerRepository.findByIdentityGuid(order.getBuyerId().toString());
        return buyer != null ? buyer.getName() : "";
    }
}
