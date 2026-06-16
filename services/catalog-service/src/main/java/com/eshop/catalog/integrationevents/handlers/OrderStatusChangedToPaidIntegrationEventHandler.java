package com.eshop.catalog.integrationevents.handlers;

import com.eshop.catalog.domain.CatalogItem;
import com.eshop.catalog.infrastructure.CatalogItemRepository;
import com.eshop.catalog.integrationevents.events.OrderStatusChangedToPaidIntegrationEvent;
import com.eshop.eventbus.IIntegrationEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles {@link OrderStatusChangedToPaidIntegrationEvent} from Ordering.
 *
 * <p>Calls {@link CatalogItem#removeStock(int)} on each purchased item to deduct
 * the sold units from inventory, then saves all changes in a single transaction.
 *
 * <p>Non-blocking stock removal — if a partial shortfall exists the available
 * quantity is reduced to zero and the remainder is silently dropped (matching
 * the .NET source comment: "we're not blocking stock/inventory").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangedToPaidIntegrationEventHandler
        implements IIntegrationEventHandler<OrderStatusChangedToPaidIntegrationEvent> {

    private final CatalogItemRepository catalogItemRepository;

    @RabbitListener(queues = "Catalog",
            id = "catalogPaidListener",
            containerFactory = "rabbitListenerContainerFactory")
    @Override
    @Transactional
    public void handle(OrderStatusChangedToPaidIntegrationEvent event) throws Exception {
        log.info("Handling integration event OrderStatusChangedToPaid (id={}, orderId={})",
                event.getId(), event.getOrderId());

        for (OrderStatusChangedToPaidIntegrationEvent.OrderStockItem orderStockItem
                : event.getOrderStockItems()) {

            var catalogItem = catalogItemRepository.findById(orderStockItem.getProductId())
                    .orElse(null);

            if (catalogItem == null) {
                log.warn("Catalog item {} not found while processing paid order {} — skipping",
                        orderStockItem.getProductId(), event.getOrderId());
                continue;
            }

            catalogItem.removeStock(orderStockItem.getUnits());
            catalogItemRepository.save(catalogItem);

            log.debug("Removed {} units of catalog item {} (order {})",
                    orderStockItem.getUnits(), catalogItem.getId(), event.getOrderId());
        }

        log.info("Stock deducted for all items in paid order {}", event.getOrderId());
    }
}
