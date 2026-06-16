package com.eshop.mobileshoppingagg.services;

import com.eshop.mobileshoppingagg.config.AggregatorConfig;
import com.eshop.mobileshoppingagg.model.BasketData;
import com.eshop.mobileshoppingagg.model.OrderData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Calls the ordering-service to build an order draft from a basket.
 *
 * <p>The caller's bearer token is forwarded so the ordering-service can
 * authenticate the request.
 */
@Slf4j
@Service
public class OrderApiClient {

    private final WebClient webClient;

    public OrderApiClient(WebClient.Builder webClientBuilder, AggregatorConfig config) {
        this.webClient = webClientBuilder
                .baseUrl(config.getOrderingUrl())
                .build();
    }

    /**
     * POSTs the basket to {@code /api/v1/orders/draft} and returns the resulting
     * {@link OrderData}.
     *
     * @param basket      the buyer's current basket
     * @param bearerToken the caller's JWT (forwarded to the ordering-service)
     * @return order draft
     */
    public OrderData getOrderDraftFromBasketAsync(BasketData basket, String bearerToken) {
        log.debug("Creating order draft from basket buyerId={}", basket.getBuyerId());
        return webClient.post()
                .uri("/api/v1/orders/draft")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .bodyValue(basket)
                .retrieve()
                .bodyToMono(OrderData.class)
                .block();
    }
}
