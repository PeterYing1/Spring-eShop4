package com.eshop.catalog.integrationevents.handlers;

import com.eshop.catalog.infrastructure.CatalogItemRepository;
import com.eshop.catalog.integrationevents.CatalogIntegrationEventService;
import com.eshop.catalog.integrationevents.events.OrderStatusChangedToAwaitingValidationIntegrationEvent;
import com.eshop.catalog.integrationevents.events.OrderStockConfirmedIntegrationEvent;
import com.eshop.catalog.integrationevents.events.OrderStockRejectedIntegrationEvent;
import com.eshop.eventbus.IIntegrationEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles {@link OrderStatusChangedToAwaitingValidationIntegrationEvent} from Ordering.
 *
 * <p>For each order stock item, this handler checks whether the catalog has
 * sufficient {@code availableStock}. It then publishes:
 * <ul>
 *   <li>{@link OrderStockConfirmedIntegrationEvent} if ALL items have sufficient stock.</li>
 *   <li>{@link OrderStockRejectedIntegrationEvent} (with a per-item stock verdict) if
 *       ANY item is under-stocked.</li>
 * </ul>
 *
 * <p>The listener is bound to the {@code Catalog} queue by the
 * {@link com.eshop.catalog.config.RabbitMQCatalogConfig} bean declarations.
 * The {@code @RabbitListener} annotation here handles message dispatch at the
 * Spring AMQP layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusChangedToAwaitingValidationIntegrationEventHandler
        implements IIntegrationEventHandler<OrderStatusChangedToAwaitingValidationIntegrationEvent> {

    private final CatalogItemRepository catalogItemRepository;
    private final CatalogIntegrationEventService catalogIntegrationEventService;

    @RabbitListener(queues = "Catalog",
            id = "catalogAwaitingValidationListener",
            containerFactory = "rabbitListenerContainerFactory")
    @Override
    public void handle(OrderStatusChangedToAwaitingValidationIntegrationEvent event) throws Exception {
        log.info("Handling integration event OrderStatusChangedToAwaitingValidation (id={}, orderId={})",
                event.getId(), event.getOrderId());

        List<OrderStockRejectedIntegrationEvent.ConfirmedOrderStockItem> confirmedOrderStockItems = new ArrayList<>();

        for (OrderStatusChangedToAwaitingValidationIntegrationEvent.OrderStockItem orderStockItem
                : event.getOrderStockItems()) {

            var catalogItem = catalogItemRepository.findById(orderStockItem.getProductId())
                    .orElse(null);

            boolean hasStock = catalogItem != null
                    && catalogItem.getAvailableStock() >= orderStockItem.getUnits();

            confirmedOrderStockItems.add(
                    new OrderStockRejectedIntegrationEvent.ConfirmedOrderStockItem(
                            orderStockItem.getProductId(), hasStock));
        }

        boolean allHaveStock = confirmedOrderStockItems.stream()
                .allMatch(OrderStockRejectedIntegrationEvent.ConfirmedOrderStockItem::isHasStock);

        if (allHaveStock) {
            log.info("All items in order {} have sufficient stock — confirming", event.getOrderId());
            var confirmedEvent = new OrderStockConfirmedIntegrationEvent(event.getOrderId());
            catalogIntegrationEventService.saveEventAndCatalogContextChangesAsync(confirmedEvent);
        } else {
            log.warn("One or more items in order {} lack sufficient stock — rejecting", event.getOrderId());
            var rejectedEvent = new OrderStockRejectedIntegrationEvent(
                    event.getOrderId(), confirmedOrderStockItems);
            catalogIntegrationEventService.saveEventAndCatalogContextChangesAsync(rejectedEvent);
        }
    }
}
