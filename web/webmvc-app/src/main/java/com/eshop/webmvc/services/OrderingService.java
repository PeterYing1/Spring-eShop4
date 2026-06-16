package com.eshop.webmvc.services;

import com.eshop.webmvc.model.OrderDetails;
import com.eshop.webmvc.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderingService {

    private final WebClient orderClient;

    public List<OrderSummary> getOrders(String accessToken) {
        log.debug("Fetching orders list");
        return orderClient.get()
                .uri("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(OrderSummary.class)
                .collectList()
                .block();
    }

    public OrderDetails getOrder(int orderId, String accessToken) {
        log.debug("Fetching order details for orderId: {}", orderId);
        return orderClient.get()
                .uri("/api/v1/orders/{orderId}", orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(OrderDetails.class)
                .block();
    }

    public boolean cancelOrder(int orderId, UUID requestId, String accessToken) {
        log.debug("Cancelling order: {}", orderId);
        try {
            orderClient.put()
                    .uri("/api/v1/orders/cancel")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("x-requestid", requestId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("orderNumber", orderId))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
}
