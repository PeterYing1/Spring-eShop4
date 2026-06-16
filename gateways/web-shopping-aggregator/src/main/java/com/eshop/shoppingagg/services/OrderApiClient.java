package com.eshop.shoppingagg.services;

import com.eshop.shoppingagg.model.BasketData;
import com.eshop.shoppingagg.model.BasketDataItem;
import com.eshop.shoppingagg.model.OrderData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calls the ordering service to create an order draft from a basket.
 *
 * <p>The draft endpoint ({@code POST /api/v1/orders/draft}) is protected — the
 * caller's JWT is forwarded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApiClient {

    @Qualifier("orderingServiceClient")
    private final WebClient orderingServiceClient;

    /**
     * Requests an order draft from the ordering service based on the current
     * basket contents.
     *
     * @param buyerId     the authenticated user's sub claim
     * @param basket      the basket whose items form the draft order
     * @param bearerToken raw JWT token value to forward as Bearer auth
     * @return an {@link OrderData} draft
     */
    public OrderData getOrderDraft(String buyerId, BasketData basket, String bearerToken) {
        log.debug("Requesting order draft for buyerId={}", buyerId);

        CreateOrderDraftRequest request = buildDraftRequest(buyerId, basket);

        return orderingServiceClient.post()
                .uri("/api/v1/orders/draft")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderData.class)
                .block();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private CreateOrderDraftRequest buildDraftRequest(String buyerId, BasketData basket) {
        List<OrderItemDto> orderItems = basket.getItems().stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());

        CreateOrderDraftRequest req = new CreateOrderDraftRequest();
        req.setBuyerId(buyerId);
        req.setOrderItems(orderItems);
        return req;
    }

    private OrderItemDto toOrderItemDto(BasketDataItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setOldUnitPrice(item.getOldUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setPictureUrl(item.getPictureUrl());
        dto.setDiscount(BigDecimal.ZERO);
        return dto;
    }

    // -------------------------------------------------------------------------
    // Inner DTOs (request body shape expected by ordering service)
    // -------------------------------------------------------------------------

    @Data
    @NoArgsConstructor
    static class CreateOrderDraftRequest {
        private String buyerId;
        private List<OrderItemDto> orderItems;
    }

    @Data
    @NoArgsConstructor
    static class OrderItemDto {
        private int productId;
        private String productName;
        private BigDecimal unitPrice;
        private BigDecimal oldUnitPrice;
        private int quantity;
        private String pictureUrl;
        private BigDecimal discount;
    }
}
