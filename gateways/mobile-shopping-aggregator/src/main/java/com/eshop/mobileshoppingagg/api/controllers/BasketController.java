package com.eshop.mobileshoppingagg.api.controllers;

import com.eshop.mobileshoppingagg.model.*;
import com.eshop.mobileshoppingagg.services.BasketService;
import com.eshop.mobileshoppingagg.services.BearerTokenExtractor;
import com.eshop.mobileshoppingagg.services.CatalogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BFF basket controller — aggregates catalog and basket data.
 *
 * <p>Key difference from the web BFF:
 * <ul>
 *   <li>{@code POST /api/v1/basket/items} — <strong>appends</strong> a new line
 *       (mobile append-only behaviour).  The web BFF increments an existing
 *       line.</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/basket")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class BasketController {

    private final CatalogService catalogService;
    private final BasketService basketService;
    private final BearerTokenExtractor bearerTokenExtractor;

    /**
     * POST or PUT /api/v1/basket
     *
     * <p>Replaces the entire basket.  Catalog data is fetched to enrich each
     * item with the current name and price.
     */
    @PostMapping
    @PutMapping
    public ResponseEntity<BasketData> updateAllBasket(
            @RequestBody UpdateBasketRequest data,
            HttpServletRequest request) {

        if (data.getItems() == null || !data.getItems().iterator().hasNext()) {
            return ResponseEntity.badRequest().build();
        }

        String token = bearerTokenExtractor.extract(request);

        // Load or create basket
        BasketData basket = basketService.getById(data.getBuyerId(), token);
        if (basket == null) {
            basket = new BasketData(data.getBuyerId());
        }

        // Fetch catalog items for enrichment
        List<Integer> productIds = new ArrayList<>();
        for (UpdateBasketRequestItemData item : data.getItems()) {
            productIds.add(item.getProductId());
        }
        List<CatalogItem> catalogItems = catalogService.getCatalogItemsAsync(productIds);
        Map<Integer, CatalogItem> catalogMap = catalogItems.stream()
                .collect(Collectors.toMap(CatalogItem::getId, ci -> ci));

        // Group by productId, summing quantities
        Map<Integer, UpdateBasketRequestItemData> grouped = new java.util.LinkedHashMap<>();
        for (UpdateBasketRequestItemData item : data.getItems()) {
            grouped.merge(item.getProductId(), item, (existing, incoming) -> {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                return existing;
            });
        }

        for (UpdateBasketRequestItemData bitem : grouped.values()) {
            CatalogItem catalogItem = catalogMap.get(bitem.getProductId());
            if (catalogItem == null) {
                log.warn("Basket refers to a non-existing catalog item ({})", bitem.getProductId());
                return ResponseEntity.badRequest().build();
            }

            BasketDataItem existing = basket.getItems().stream()
                    .filter(i -> i.getProductId() == bitem.getProductId())
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                BasketDataItem newItem = new BasketDataItem();
                newItem.setId(bitem.getId());
                newItem.setProductId(catalogItem.getId());
                newItem.setProductName(catalogItem.getName());
                newItem.setPictureUrl(catalogItem.getPictureUri());
                newItem.setUnitPrice(catalogItem.getPrice());
                newItem.setOldUnitPrice(java.math.BigDecimal.ZERO);
                newItem.setQuantity(bitem.getQuantity());
                basket.getItems().add(newItem);
            } else {
                existing.setQuantity(bitem.getQuantity());
            }
        }

        BasketData updated = basketService.updateAsync(basket, token);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/v1/basket/items
     *
     * <p>Updates quantities of existing basket lines without changing other
     * attributes.
     */
    @PutMapping("/items")
    public ResponseEntity<BasketData> updateQuantities(
            @RequestBody UpdateBasketItemsRequest data,
            HttpServletRequest request) {

        if (data.getUpdates() == null || data.getUpdates().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String token = bearerTokenExtractor.extract(request);

        BasketData currentBasket = basketService.getById(data.getBasketId(), token);
        if (currentBasket == null) {
            log.warn("Basket with id {} not found", data.getBasketId());
            return ResponseEntity.badRequest().build();
        }

        for (UpdateBasketItemData update : data.getUpdates()) {
            BasketDataItem basketItem = currentBasket.getItems().stream()
                    .filter(i -> i.getId() != null && i.getId().equals(update.getBasketItemId()))
                    .findFirst()
                    .orElse(null);

            if (basketItem == null) {
                log.warn("Basket item with id {} not found", update.getBasketItemId());
                return ResponseEntity.badRequest().build();
            }
            basketItem.setQuantity(update.getNewQty());
        }

        BasketData updated = basketService.updateAsync(currentBasket, token);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/v1/basket/items
     *
     * <p>Mobile BFF <strong>append-only</strong> behaviour: always adds a new
     * basket line regardless of whether the product already exists.
     */
    @PostMapping("/items")
    public ResponseEntity<Void> addBasketItem(
            @RequestBody AddBasketItemRequest data,
            HttpServletRequest request) {

        if (data == null || data.getQuantity() == 0) {
            return ResponseEntity.badRequest().build();
        }

        String token = bearerTokenExtractor.extract(request);

        // Fetch catalog item for price / name enrichment
        CatalogItem item = catalogService.getCatalogItemAsync(data.getCatalogItemId());
        if (item == null) {
            log.warn("Catalog item {} not found", data.getCatalogItemId());
            return ResponseEntity.badRequest().build();
        }

        BasketData currentBasket = basketService.getById(data.getBasketId(), token);
        if (currentBasket == null) {
            currentBasket = new BasketData(data.getBasketId());
        }

        // Append-only: always add a new line
        BasketDataItem newItem = new BasketDataItem();
        newItem.setId(UUID.randomUUID().toString());
        newItem.setProductId(item.getId());
        newItem.setProductName(item.getName());
        newItem.setPictureUrl(item.getPictureUri());
        newItem.setUnitPrice(item.getPrice());
        newItem.setOldUnitPrice(java.math.BigDecimal.ZERO);
        newItem.setQuantity(data.getQuantity());
        currentBasket.getItems().add(newItem);

        basketService.updateAsync(currentBasket, token);
        return ResponseEntity.ok().build();
    }
}
