package com.eshop.shoppingagg.api.controllers;

import com.eshop.shoppingagg.model.BasketData;
import com.eshop.shoppingagg.model.OrderData;
import com.eshop.shoppingagg.services.BasketService;
import com.eshop.shoppingagg.services.OrderApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * BFF aggregator controller for order draft operations.
 *
 * <p>{@code GET /api/v1/order/draft} loads the authenticated user's basket and
 * asks the ordering service to project it into an {@link OrderData} draft
 * (pricing, totals, etc.) without creating a real order.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class OrderController {

    private final BasketService basketService;
    private final OrderApiClient orderApiClient;

    /**
     * Returns an order draft derived from the currently authenticated user's
     * basket.
     *
     * <p>Returns {@code 400 Bad Request} if the basket does not exist or the
     * basket ID is blank.
     */
    @GetMapping("/draft")
    public ResponseEntity<?> getOrderDraft() {
        JwtAuthenticationToken auth =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getToken().getSubject();
        String bearerToken = auth.getToken().getTokenValue();

        log.debug("GET order draft for userId={}", userId);

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("Could not resolve authenticated user identity");
        }

        BasketData basket = basketService.getBasket(userId, bearerToken);
        if (basket == null) {
            return ResponseEntity.badRequest().body("No basket found for user " + userId);
        }

        OrderData draft = orderApiClient.getOrderDraft(userId, basket, bearerToken);
        return ResponseEntity.ok(draft);
    }
}
