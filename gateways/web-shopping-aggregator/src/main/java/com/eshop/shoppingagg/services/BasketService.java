package com.eshop.shoppingagg.services;

import com.eshop.shoppingagg.model.BasketData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Calls the basket service to read and update customer baskets.
 *
 * <p>All calls forward the caller's JWT Bearer token to the basket service,
 * which is itself a secured endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasketService {

    @Qualifier("basketServiceClient")
    private final WebClient basketServiceClient;

    /**
     * Retrieves the basket for the given user ID.
     *
     * @param userId      the buyer / user identifier
     * @param bearerToken the raw JWT token value (without the "Bearer " prefix)
     * @return the {@link BasketData}, or {@code null} if no basket exists
     */
    public BasketData getBasket(String userId, String bearerToken) {
        log.debug("Getting basket for user={}", userId);
        return basketServiceClient.get()
                .uri("/api/v1/basket/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .bodyToMono(BasketData.class)
                .block();
    }

    /**
     * Creates or replaces the basket.
     *
     * @param basket      the basket to persist
     * @param bearerToken the raw JWT token value
     * @return the updated {@link BasketData} as returned by the basket service
     */
    public BasketData updateBasket(BasketData basket, String bearerToken) {
        log.debug("Updating basket for buyerId={}", basket.getBuyerId());
        return basketServiceClient.post()
                .uri("/api/v1/basket")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .bodyValue(basket)
                .retrieve()
                .bodyToMono(BasketData.class)
                .block();
    }

    /**
     * Deletes the basket for the given user ID.
     *
     * @param userId      the buyer identifier
     * @param bearerToken the raw JWT token value
     */
    public void deleteBasket(String userId, String bearerToken) {
        log.debug("Deleting basket for user={}", userId);
        basketServiceClient.delete()
                .uri("/api/v1/basket/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
