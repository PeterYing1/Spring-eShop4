package com.eshop.basket.infrastructure;

import com.eshop.basket.domain.CustomerBasket;
import com.eshop.basket.domain.IBasketRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis-backed implementation of {@link IBasketRepository}.
 *
 * <p>Baskets are stored as camelCase JSON strings. The Redis key is the
 * buyer's user identifier ({@code buyerId}).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisBasketRepository implements IBasketRepository {

    private final StringRedisTemplate redisTemplate;

    /**
     * Default Jackson ObjectMapper (camelCase) injected by Spring — used for
     * basket JSON storage. The integration-event ObjectMapper (PascalCase) is
     * a separate bean and is NOT used here.
     */
    private final ObjectMapper objectMapper;

    @Override
    public CustomerBasket getBasket(String customerId) {
        String json = redisTemplate.opsForValue().get(customerId);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CustomerBasket.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise basket for customerId '{}': {}", customerId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public CustomerBasket updateBasket(CustomerBasket basket) {
        try {
            String json = objectMapper.writeValueAsString(basket);
            redisTemplate.opsForValue().set(basket.getBuyerId(), json);
            log.info("Basket persisted successfully for buyerId '{}'", basket.getBuyerId());
            return basket;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise basket for buyerId '{}': {}", basket.getBuyerId(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void deleteBasket(String id) {
        redisTemplate.delete(id);
    }
}
