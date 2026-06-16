package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import com.eshop.ordering.integrationevents.events.OrderStatusChangedToCancelledIntegrationEvent;
import com.eshop.ordering.integrationevents.events.OrderStockRejectedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles {@link OrderStockRejectedIntegrationEvent} from the Catalog service.
 *
 * <p>Cancels the order for rejected stock items and publishes the cancelled
 * integration event.
 */
@Component
public class OrderStockRejectedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            OrderStockRejectedIntegrationEventHandler.class);

    private final IOrderRepository orderRepository;
    private final IBuyerRepository buyerRepository;
    private final IEventBus eventBus;

    public OrderStockRejectedIntegrationEventHandler(
            IOrderRepository orderRepository,
            IBuyerRepository buyerRepository,
            IEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    public void handle(OrderStockRejectedIntegrationEvent event) throws Exception {
        log.info("Handling OrderStockRejectedIntegrationEvent for order #{}", event.getOrderId());

        Order order = orderRepository.get(event.getOrderId());
        if (order == null) {
            log.warn("Order #{} not found", event.getOrderId());
            return;
        }

        List<Integer> rejectedIds = event.getOrderStockItems().stream()
                .filter(item -> !item.isHasStock())
                .map(OrderStockRejectedIntegrationEvent.ConfirmedOrderStockItem::getProductId)
                .collect(Collectors.toList());

        order.setCancelledStatusWhenStockIsRejected(rejectedIds);
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
