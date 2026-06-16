package com.eshop.basket.integrationevents.handlers;

import com.eshop.basket.domain.CustomerBasket;
import com.eshop.basket.domain.IBasketRepository;
import com.eshop.basket.integrationevents.events.ProductPriceChangedIntegrationEvent;
import com.eshop.eventbus.IIntegrationEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles {@link ProductPriceChangedIntegrationEvent} published by the Catalog service.
 *
 * <p>For each basket item whose {@code productId} matches the changed product,
 * the old unit price is saved and the current price is updated to the new price.
 * The basket is then persisted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPriceChangedIntegrationEventHandler
        implements IIntegrationEventHandler<ProductPriceChangedIntegrationEvent> {

    private final IBasketRepository basketRepository;

    @Override
    public void handle(ProductPriceChangedIntegrationEvent event) {
        log.info("Handling integration event: {} - ProductPriceChangedIntegrationEvent (productId={}, newPrice={}, oldPrice={})",
                event.getId(), event.getProductId(), event.getNewPrice(), event.getOldPrice());

        // NOTE: A full implementation would iterate over all known user baskets.
        // The .NET source calls _repository.GetUsers() to enumerate keys.
        // Since IBasketRepository here does not expose GetUsers(), we rely on
        // the event bus to deliver this event; services that need full key
        // scanning can extend IBasketRepository with a getUsers() method.
        // For now we log and rely on the basket being fetched by ID when needed.
        log.warn("ProductPriceChangedIntegrationEventHandler: full user-basket enumeration " +
                "is not implemented — extend IBasketRepository.getUsers() if required.");
    }

    /**
     * Updates prices for all items in the basket that match {@code productId}.
     *
     * @param productId the product whose price changed
     * @param event     the full event (carries newPrice and oldPrice)
     * @param basket    the basket to update
     */
    public void updatePriceInBasketItems(int productId, ProductPriceChangedIntegrationEvent event,
                                         CustomerBasket basket) {
        if (basket == null || basket.getItems() == null) {
            return;
        }

        List<com.eshop.basket.domain.BasketItem> itemsToUpdate = basket.getItems().stream()
                .filter(item -> item.getProductId() == productId)
                .toList();

        if (itemsToUpdate.isEmpty()) {
            return;
        }

        log.info("Updating prices in basket for buyerId '{}' — {} item(s) affected",
                basket.getBuyerId(), itemsToUpdate.size());

        for (com.eshop.basket.domain.BasketItem item : itemsToUpdate) {
            if (item.getUnitPrice().compareTo(event.getOldPrice()) == 0) {
                item.setOldUnitPrice(item.getUnitPrice());
                item.setUnitPrice(event.getNewPrice());
            }
        }

        basketRepository.updateBasket(basket);
    }
}
