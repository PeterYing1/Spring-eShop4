package com.eshop.shoppingagg.api.controllers;

import com.eshop.shoppingagg.model.BasketData;
import com.eshop.shoppingagg.model.BasketDataItem;
import com.eshop.shoppingagg.model.CatalogItem;
import com.eshop.shoppingagg.model.UpdateBasketRequest;
import com.eshop.shoppingagg.model.UpdateBasketRequestItemData;
import com.eshop.shoppingagg.services.BasketService;
import com.eshop.shoppingagg.services.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BFF aggregator controller for basket operations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>{@code GET  /{id}}  — fetch basket from the basket service, enrich each
 *       item with current catalog data (name, price, picture).</li>
 *   <li>{@code POST /}      — for every item in the request, look up the current
 *       catalog price, build an enriched basket and persist it via the basket
 *       service.</li>
 *   <li>{@code DELETE /{id}} — proxy delete to the basket service.</li>
 * </ul>
 *
 * <p>All endpoints require an authenticated JWT.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/basket")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class BasketController {

    private final CatalogService catalogService;
    private final BasketService basketService;

    // -------------------------------------------------------------------------
    // GET /{id} — retrieve and enrich basket
    // -------------------------------------------------------------------------

    /**
     * Returns the basket for {@code id}, with each item enriched with the
     * latest catalog name, price, and picture URL.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BasketData> getBasketById(@PathVariable String id) {
        log.debug("GET basket id={}", id);
        String token = extractBearerToken();

        BasketData basket = basketService.getBasket(id, token);
        if (basket == null) {
            basket = new BasketData(id);
        }

        // Enrich each item with current catalog data
        for (BasketDataItem item : basket.getItems()) {
            CatalogItem catalogItem = catalogService.getItemById(item.getProductId());
            if (catalogItem != null) {
                item.setProductName(catalogItem.getName());
                item.setUnitPrice(catalogItem.getPrice());
                item.setPictureUrl(catalogItem.getPictureUri());
            }
        }

        return ResponseEntity.ok(basket);
    }

    // -------------------------------------------------------------------------
    // POST / — create/update basket
    // -------------------------------------------------------------------------

    /**
     * Replaces the basket for the buyer identified in {@code data.buyerId}.
     *
     * <p>For each product in the request the current catalog price and metadata
     * are fetched, then the basket is saved via the basket service.
     *
     * <p>Items with the same {@code productId} are merged (quantities summed).
     *
     * @return {@code 400} if items list is empty or a catalog item is not found;
     *         otherwise {@code 200} with the updated basket.
     */
    @PostMapping
    public ResponseEntity<?> updateBasket(@RequestBody UpdateBasketRequest data) {
        log.debug("POST basket buyerId={}", data.getBuyerId());

        if (data.getItems() == null || data.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Need to pass at least one basket line");
        }

        String token = extractBearerToken();

        // Fetch or create current basket
        BasketData basket = basketService.getBasket(data.getBuyerId(), token);
        if (basket == null) {
            basket = new BasketData(data.getBuyerId());
        }

        // Group by productId: sum quantities for duplicates
        List<UpdateBasketRequestItemData> deduped = mergeByProductId(data.getItems());

        List<BasketDataItem> updatedItems = new ArrayList<>(basket.getItems());

        for (UpdateBasketRequestItemData reqItem : deduped) {
            CatalogItem catalogItem = catalogService.getItemById(reqItem.getProductId());
            if (catalogItem == null) {
                return ResponseEntity.badRequest()
                        .body("Basket refers to a non-existing catalog item (" + reqItem.getProductId() + ")");
            }

            // Find existing item in basket
            BasketDataItem existing = updatedItems.stream()
                    .filter(i -> i.getProductId() == reqItem.getProductId())
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                // Add new item
                BasketDataItem newItem = new BasketDataItem();
                newItem.setId(reqItem.getId() != null && !reqItem.getId().isBlank()
                        ? reqItem.getId()
                        : UUID.randomUUID().toString());
                newItem.setProductId(catalogItem.getId());
                newItem.setProductName(catalogItem.getName());
                newItem.setPictureUrl(catalogItem.getPictureUri());
                newItem.setUnitPrice(catalogItem.getPrice());
                newItem.setQuantity(reqItem.getQuantity());
                updatedItems.add(newItem);
            } else {
                // Update quantity and refresh catalog data
                existing.setQuantity(reqItem.getQuantity());
                existing.setProductName(catalogItem.getName());
                existing.setUnitPrice(catalogItem.getPrice());
                existing.setPictureUrl(catalogItem.getPictureUri());
            }
        }

        basket.setItems(updatedItems);
        BasketData savedBasket = basketService.updateBasket(basket, token);
        return ResponseEntity.ok(savedBasket);
    }

    // -------------------------------------------------------------------------
    // DELETE /{id} — delete basket
    // -------------------------------------------------------------------------

    /**
     * Deletes the basket identified by {@code id}.
     *
     * @return {@code 200 OK}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBasket(@PathVariable String id) {
        log.debug("DELETE basket id={}", id);
        String token = extractBearerToken();
        basketService.deleteBasket(id, token);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String extractBearerToken() {
        JwtAuthenticationToken auth =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return auth.getToken().getTokenValue();
    }

    /**
     * Merges items with the same {@code productId} by summing their quantities.
     */
    private List<UpdateBasketRequestItemData> mergeByProductId(
            List<UpdateBasketRequestItemData> items) {
        List<UpdateBasketRequestItemData> result = new ArrayList<>();
        for (UpdateBasketRequestItemData item : items) {
            UpdateBasketRequestItemData existing = result.stream()
                    .filter(r -> r.getProductId() == item.getProductId())
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                // Defensive copy
                UpdateBasketRequestItemData copy = new UpdateBasketRequestItemData(
                        item.getId(), item.getProductId(), item.getQuantity());
                result.add(copy);
            } else {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            }
        }
        return result;
    }
}
