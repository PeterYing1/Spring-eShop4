package com.eshop.mobileshoppingagg.api.controllers;

import com.eshop.mobileshoppingagg.model.BasketData;
import com.eshop.mobileshoppingagg.model.OrderData;
import com.eshop.mobileshoppingagg.services.BasketService;
import com.eshop.mobileshoppingagg.services.BearerTokenExtractor;
import com.eshop.mobileshoppingagg.services.OrderApiClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * BFF order controller — builds an order draft from the user's basket.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class OrderController {

    private final BasketService basketService;
    private final OrderApiClient orderApiClient;
    private final BearerTokenExtractor bearerTokenExtractor;

    /**
     * GET /api/v1/order/draft/{basketId}
     *
     * <p>Retrieves the basket and forwards it to the ordering-service to create
     * a draft order.
     *
     * @param basketId basket / buyer id
     * @return {@link OrderData} draft or {@code 400 Bad Request}
     */
    @GetMapping("/draft/{basketId}")
    public ResponseEntity<OrderData> getOrderDraft(
            @PathVariable String basketId,
            HttpServletRequest request) {

        if (!StringUtils.hasText(basketId)) {
            return ResponseEntity.badRequest().build();
        }

        String token = bearerTokenExtractor.extract(request);

        BasketData basket = basketService.getById(basketId, token);
        if (basket == null) {
            log.warn("No basket found for id {}", basketId);
            return ResponseEntity.badRequest().build();
        }

        log.debug("Building order draft for basket {}", basketId);
        OrderData draft = orderApiClient.getOrderDraftFromBasketAsync(basket, token);
        return ResponseEntity.ok(draft);
    }
}
