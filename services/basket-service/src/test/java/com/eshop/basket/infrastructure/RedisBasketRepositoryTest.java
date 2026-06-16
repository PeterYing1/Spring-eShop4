package com.eshop.basket.infrastructure;

import com.eshop.basket.domain.BasketItem;
import com.eshop.basket.domain.CustomerBasket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RedisBasketRepository}.
 *
 * <p>Redis interactions are fully mocked — no running Redis instance is required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisBasketRepository")
class RedisBasketRepositoryTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;

    private RedisBasketRepository repository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Wire value operations stub so opsForValue() is available for all tests
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        repository = new RedisBasketRepository(stringRedisTemplate, objectMapper);
    }

    // -------------------------------------------------------------------------
    // getBasket
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getBasket returns deserialized basket when key exists in Redis")
    void getBasket_existingKey_returnsDeserializedBasket() throws JsonProcessingException {
        CustomerBasket expected = new CustomerBasket("user-1");
        BasketItem item = new BasketItem("i1", 10, "Widget", new BigDecimal("9.99"),
                new BigDecimal("12.99"), 3, "http://pic.url");
        expected.setItems(List.of(item));

        String json = objectMapper.writeValueAsString(expected);
        when(valueOperations.get("user-1")).thenReturn(json);

        CustomerBasket result = repository.getBasket("user-1");

        assertThat(result).isNotNull();
        assertThat(result.getBuyerId()).isEqualTo("user-1");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("Widget");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("getBasket returns null when key is absent in Redis")
    void getBasket_missingKey_returnsNull() {
        when(valueOperations.get("unknown-user")).thenReturn(null);

        CustomerBasket result = repository.getBasket("unknown-user");

        assertThat(result).isNull();
    }

    // -------------------------------------------------------------------------
    // updateBasket
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateBasket serializes the basket and writes it to Redis under the buyerId key")
    void updateBasket_serializes_andSetsInRedis() throws JsonProcessingException {
        CustomerBasket basket = new CustomerBasket("buyer-7");
        BasketItem item = new BasketItem("i2", 42, "Gadget", new BigDecimal("49.99"),
                new BigDecimal("59.99"), 1, "http://gadget.url");
        basket.setItems(List.of(item));

        CustomerBasket returned = repository.updateBasket(basket);

        // Capture what was written to Redis
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), jsonCaptor.capture());

        assertThat(returned).isSameAs(basket);
        assertThat(keyCaptor.getValue()).isEqualTo("buyer-7");

        // The stored JSON must round-trip back to the same basket
        CustomerBasket stored = objectMapper.readValue(jsonCaptor.getValue(), CustomerBasket.class);
        assertThat(stored.getBuyerId()).isEqualTo("buyer-7");
        assertThat(stored.getItems()).hasSize(1);
        assertThat(stored.getItems().get(0).getProductName()).isEqualTo("Gadget");
    }

    // -------------------------------------------------------------------------
    // deleteBasket
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteBasket delegates to StringRedisTemplate.delete with the given id")
    void deleteBasket_callsRedisDelete() {
        repository.deleteBasket("buyer-to-remove");

        verify(stringRedisTemplate).delete("buyer-to-remove");
        // opsForValue() should NOT be called during a delete
        verify(valueOperations, never()).get(any());
        verify(valueOperations, never()).set(any(), any());
    }
}
