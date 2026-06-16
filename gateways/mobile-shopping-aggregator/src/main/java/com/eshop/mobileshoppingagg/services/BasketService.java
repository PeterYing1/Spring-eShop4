package com.eshop.mobileshoppingagg.services;

import com.eshop.mobileshoppingagg.config.AggregatorConfig;
import com.eshop.mobileshoppingagg.model.BasketData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Calls the basket-service REST API on behalf of the authenticated user.
 *
 * <p>The caller's bearer token is propagated on every request so the downstream
 * basket-service can authorise the call.  All calls use {@link WebClient#block()}
 * because the aggregator runs in a servlet (blocking) context.
 */
@Slf4j
@Service
public class BasketService {

    private final WebClient webClient;

    public BasketService(WebClient.Builder webClientBuilder, AggregatorConfig config) {
        this.webClient = webClientBuilder
                .baseUrl(config.getBasketUrl())
                .build();
    }

    /**
     * Retrieves the basket for the given {@code id}.
     *
     * @param id       basket / buyer id
     * @param bearerToken the caller's JWT (forwarded to the basket-service)
     * @return {@link BasketData} or {@code null} if not found
     */
    public BasketData getById(String id, String bearerToken) {
        log.debug("Getting basket id={}", id);
        return webClient.get()
                .uri("/api/v1/basket/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(BasketData.class)
                .onErrorResume(e -> {
                    log.warn("Basket not found for id={}: {}", id, e.getMessage());
                    return Mono.empty();
                })
                .block();
    }

    /**
     * Creates or replaces the basket.
     *
     * @param basket      updated basket
     * @param bearerToken the caller's JWT
     */
    public BasketData updateAsync(BasketData basket, String bearerToken) {
        log.debug("Updating basket buyerId={}", basket.getBuyerId());
        return webClient.post()
                .uri("/api/v1/basket")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .bodyValue(basket)
                .retrieve()
                .bodyToMono(BasketData.class)
                .block();
    }
}
