package com.eshop.basket.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CustomerBasket}.
 *
 * <p>Plain JUnit 5 — no Spring context required.
 */
@DisplayName("CustomerBasket")
class CustomerBasketTest {

    @Test
    @DisplayName("new basket created with buyerId has an empty items list")
    void createBasket_withBuyerId_hasEmptyItems() {
        CustomerBasket basket = new CustomerBasket("buyer-42");

        assertThat(basket.getBuyerId()).isEqualTo("buyer-42");
        assertThat(basket.getItems()).isNotNull();
        assertThat(basket.getItems()).isEmpty();
    }

    @Test
    @DisplayName("no-arg constructor produces a basket with null buyerId and empty items list")
    void createBasket_defaultConstructor_works() {
        CustomerBasket basket = new CustomerBasket();

        assertThat(basket.getBuyerId()).isNull();
        assertThat(basket.getItems()).isNotNull();
        assertThat(basket.getItems()).isEmpty();
    }

    @Test
    @DisplayName("item added to basket is present in the items list")
    void addItem_toBasket_itemIsPresent() {
        CustomerBasket basket = new CustomerBasket("buyer-99");
        BasketItem item = new BasketItem(
                "item-1",
                101,
                "Blue T-Shirt",
                new BigDecimal("19.99"),
                new BigDecimal("24.99"),
                2,
                "http://example.com/pic.jpg"
        );

        basket.getItems().add(item);

        assertThat(basket.getItems()).hasSize(1);
        assertThat(basket.getItems().get(0).getProductName()).isEqualTo("Blue T-Shirt");
        assertThat(basket.getItems().get(0).getQuantity()).isEqualTo(2);
    }
}
