package com.eshop.basket.api;

import com.eshop.basket.domain.BasketCheckout;
import com.eshop.basket.domain.BasketItem;
import com.eshop.basket.domain.CustomerBasket;
import com.eshop.basket.domain.IBasketRepository;
import com.eshop.eventbus.IEventBus;
import com.eshop.security.IIdentityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link BasketController}.
 *
 * <p>Security filters are disabled ({@code addFilters = false}) so that tests
 * can focus on controller logic without wiring a full JWT resource server.
 * A mock authentication is placed directly into the {@link SecurityContextHolder}
 * for tests that need an authenticated user.
 */
@WebMvcTest(BasketController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BasketController")
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IBasketRepository basketRepository;

    @MockBean
    private IIdentityService identityService;

    @MockBean
    private IEventBus eventBus;

    @BeforeEach
    void setUpSecurityContext() {
        // Place a simple authenticated principal so getName() works in checkout
        var auth = new UsernamePasswordAuthenticationToken(
                "user-1", "user-1",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    // -------------------------------------------------------------------------
    // GET /{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /{id} returns 200 OK with the stored basket")
    void getBasket_returnsBasket() throws Exception {
        CustomerBasket basket = buildBasket("user-1");
        when(basketRepository.getBasket("user-1")).thenReturn(basket);

        mockMvc.perform(get("/api/v1/basket/user-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.buyerId").value("user-1"))
                .andExpect(jsonPath("$.items[0].productName").value("Test Widget"));
    }

    @Test
    @DisplayName("GET /{id} returns 200 OK with an empty basket when none is stored")
    void getBasket_noExistingBasket_returnsEmptyBasket() throws Exception {
        when(basketRepository.getBasket("new-user")).thenReturn(null);

        mockMvc.perform(get("/api/v1/basket/new-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyerId").value("new-user"))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // -------------------------------------------------------------------------
    // POST /
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST / with a valid basket body returns 200 OK with the updated basket")
    void updateBasket_validBasket_returnsUpdated() throws Exception {
        CustomerBasket basket = buildBasket("user-1");
        when(basketRepository.updateBasket(any(CustomerBasket.class))).thenReturn(basket);

        mockMvc.perform(post("/api/v1/basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(basket)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyerId").value("user-1"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(9.99));

        verify(basketRepository).updateBasket(any(CustomerBasket.class));
    }

    // -------------------------------------------------------------------------
    // POST /checkout
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /checkout returns 202 Accepted when a basket exists for the authenticated user")
    void checkout_validCheckout_returns202() throws Exception {
        CustomerBasket basket = buildBasket("user-1");
        when(identityService.getUserIdentity()).thenReturn("user-1");
        when(basketRepository.getBasket("user-1")).thenReturn(basket);

        BasketCheckout checkout = buildCheckout();

        mockMvc.perform(post("/api/v1/basket/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkout)))
                .andExpect(status().isAccepted());

        verify(eventBus).publish(any());
    }

    @Test
    @DisplayName("POST /checkout returns 400 Bad Request when no basket exists for the user")
    void checkout_missingBasket_returns400() throws Exception {
        when(identityService.getUserIdentity()).thenReturn("user-1");
        when(basketRepository.getBasket("user-1")).thenReturn(null);

        BasketCheckout checkout = buildCheckout();

        mockMvc.perform(post("/api/v1/basket/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkout)))
                .andExpect(status().isBadRequest());

        verify(eventBus, never()).publish(any());
    }

    // -------------------------------------------------------------------------
    // DELETE /{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /{id} returns 200 OK and delegates to the repository")
    void deleteBasket_calls200() throws Exception {
        mockMvc.perform(delete("/api/v1/basket/user-1"))
                .andExpect(status().isOk());

        verify(basketRepository).deleteBasket("user-1");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CustomerBasket buildBasket(String buyerId) {
        BasketItem item = new BasketItem(
                "item-abc",
                55,
                "Test Widget",
                new BigDecimal("9.99"),
                new BigDecimal("12.99"),
                1,
                "http://example.com/widget.jpg"
        );
        CustomerBasket basket = new CustomerBasket(buyerId);
        basket.setItems(List.of(item));
        return basket;
    }

    private BasketCheckout buildCheckout() {
        BasketCheckout checkout = new BasketCheckout();
        checkout.setCity("Seattle");
        checkout.setStreet("123 Main St");
        checkout.setState("WA");
        checkout.setCountry("USA");
        checkout.setZipCode("98101");
        checkout.setCardNumber("4111111111111111");
        checkout.setCardHolderName("Test User");
        checkout.setCardExpiration(Instant.parse("2027-12-31T00:00:00Z"));
        checkout.setCardSecurityNumber("123");
        checkout.setCardTypeId(1);
        checkout.setBuyer("user-1");
        checkout.setRequestId(UUID.randomUUID());
        return checkout;
    }
}
