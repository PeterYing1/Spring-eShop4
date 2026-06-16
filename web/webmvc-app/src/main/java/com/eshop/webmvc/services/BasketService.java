package com.eshop.webmvc.services;

import com.eshop.webmvc.model.BasketCheckout;
import com.eshop.webmvc.model.CustomerBasket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketService {

    private final WebClient basketClient;

    public CustomerBasket getBasket(String userId, String accessToken) {
        log.debug("Fetching basket for user: {}", userId);
        CustomerBasket basket = basketClient.get()
                .uri("/api/v1/basket/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(CustomerBasket.class)
                .block();
        if (basket == null) {
            basket = new CustomerBasket();
            basket.setBuyerId(userId);
        }
        return basket;
    }

    public CustomerBasket updateBasket(CustomerBasket basket, String accessToken) {
        log.debug("Updating basket for buyer: {}", basket.getBuyerId());
        return basketClient.post()
                .uri("/api/v1/basket")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(basket)
                .retrieve()
                .bodyToMono(CustomerBasket.class)
                .block();
    }

    public void checkout(BasketCheckout checkout, String accessToken) {
        log.debug("Checking out basket for buyer: {}", checkout.getBuyer());
        basketClient.post()
                .uri("/api/v1/basket/checkout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(checkout)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
