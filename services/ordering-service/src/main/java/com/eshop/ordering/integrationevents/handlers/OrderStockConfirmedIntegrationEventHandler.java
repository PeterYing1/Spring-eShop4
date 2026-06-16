package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.integrationevents.events.OrderStockConfirmedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link OrderStockConfirmedIntegrationEvent} from the Catalog service.
 *
 * <p>Transitions the order to STOCK_CONFIRMED and publishes the integration
 * event consumed by the Payment service.
 */
@Component
public class OrderStockConfirmedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OrderStockConfirmedIntegrationEventHandler.class);

    private final IOrderRepository orderRepository;
    private final IEventBus eventBus;

    public OrderStockConfirmedIntegrationEventHandler(
            IOrderRepository orderRepository,
            IEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void handle(OrderStockConfirmedIntegrationEvent event) throws Exception {
        log.info("Handling OrderStockConfirmedIntegrationEvent for order #{}", event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order #{} not found", event.getOrderId());
            return;
        }

        order.setStockConfirmedStatus();
        orderRepository.update(order);

        eventBus.publish(new OrderStatusChangedToStockConfirmedIntegrationEvent(order.getId()));
    }
}
