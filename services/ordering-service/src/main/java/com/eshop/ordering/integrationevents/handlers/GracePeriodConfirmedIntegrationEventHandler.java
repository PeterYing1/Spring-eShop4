package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.domain.events.OrderStatusChangedToAwaitingValidationDomainEvent;
import com.eshop.ordering.integrationevents.events.GracePeriodConfirmedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles {@link GracePeriodConfirmedIntegrationEvent}.
 *
 * <p>Transitions the order to AWAITING_VALIDATION and publishes the
 * corresponding integration event so the Catalog service checks stock.
 */
@Component
public class GracePeriodConfirmedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            GracePeriodConfirmedIntegrationEventHandler.class);

    private final IOrderRepository orderRepository;
    private final IBuyerRepository buyerRepository;
    private final IEventBus eventBus;

    public GracePeriodConfirmedIntegrationEventHandler(
            IOrderRepository orderRepository,
            IBuyerRepository buyerRepository,
            IEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void handle(GracePeriodConfirmedIntegrationEvent event) throws Exception {
        log.info("Handling GracePeriodConfirmedIntegrationEvent for order #{}", event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order #{} not found", event.getOrderId());
            return;
        }

        order.setAwaitingValidationStatus();
        orderRepository.update(order);

        // Resolve buyer name for notification
        String buyerName = "";
        if (order.getBuyerId() != null) {
            var buyer = buyerRepository.findByIdentityGuid(order.getBuyerId().toString());
            if (buyer != null) {
                buyerName = buyer.getName();
            }
        }

        // Build stock items list from domain event
        List<OrderStatusChangedToAwaitingValidationIntegrationEvent.OrderStockItem> stockItems =
                order.getOrderItems().stream()
                        .map(i -> new OrderStatusChangedToAwaitingValidationIntegrationEvent.OrderStockItem(
                                i.getProductId(), i.getUnits()))
                        .collect(Collectors.toList());

        eventBus.publish(new OrderStatusChangedToAwaitingValidationIntegrationEvent(
                order.getId(),
                order.getOrderStatus().getName(),
                buyerName,
                stockItems));
    }
}
