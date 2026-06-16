package com.eshop.basket.api;

import com.eshop.basket.domain.BasketCheckout;
import com.eshop.basket.domain.CustomerBasket;
import com.eshop.basket.domain.IBasketRepository;
import com.eshop.basket.integrationevents.events.UserCheckoutAcceptedIntegrationEvent;
import com.eshop.eventbus.IEventBus;
import com.eshop.security.IIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for basket operations.
 *
 * <p>Mirrors the .NET {@code BasketController} behaviour:
 * <ul>
 *   <li>GET  {@code /{id}}      — retrieve basket (creates an empty one if absent)</li>
 *   <li>POST {@code /}          — create or update a basket</li>
 *   <li>POST {@code /checkout}  — initiate checkout; publishes a
 *       {@link UserCheckoutAcceptedIntegrationEvent}</li>
 *   <li>DELETE {@code /{id}}    — delete basket (returns 200 OK)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/basket")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class BasketController {

    private final IBasketRepository basketRepository;
    private final IIdentityService identityService;
    private final IEventBus eventBus;

    /**
     * Returns the basket for the given {@code id}.
     * If no basket exists an empty one is returned (never 404).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerBasket> getBasketById(@PathVariable String id) {
        CustomerBasket basket = basketRepository.getBasket(id);
        return ResponseEntity.ok(basket != null ? basket : new CustomerBasket(id));
    }

    /**
     * Creates or replaces the basket.
     *
     * @param basket basket payload; items must have quantity &gt;= 1
     */
    @PostMapping
    public ResponseEntity<CustomerBasket> updateBasket(@Valid @RequestBody CustomerBasket basket) {
        return ResponseEntity.ok(basketRepository.updateBasket(basket));
    }

    /**
     * Initiates checkout for the currently authenticated user.
     *
     * <p>Returns {@code 400 Bad Request} if no basket is found for the user.
     * On success, publishes a {@link UserCheckoutAcceptedIntegrationEvent} and
     * returns {@code 202 Accepted}.
     *
     * @param basketCheckout checkout details (address + payment)
     * @param requestId      optional idempotency key from the {@code x-requestid} header
     */
    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(
            @RequestBody BasketCheckout basketCheckout,
            @RequestHeader(value = "x-requestid", required = false) String requestId) {

        String userId = identityService.getUserIdentity();

        // Override requestId from header if it's a valid non-empty UUID
        if (requestId != null && !requestId.isBlank()) {
            try {
                UUID parsedId = UUID.fromString(requestId);
                if (!parsedId.equals(new UUID(0L, 0L))) {
                    basketCheckout.setRequestId(parsedId);
                }
            } catch (IllegalArgumentException ignored) {
                // header value was not a valid UUID — keep the body's requestId
            }
        }

        CustomerBasket basket = basketRepository.getBasket(userId);
        if (basket == null) {
            return ResponseEntity.badRequest().build();
        }

        // Obtain the user's display name from the security context
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        UserCheckoutAcceptedIntegrationEvent eventMessage = new UserCheckoutAcceptedIntegrationEvent(
                userId,
                userName,
                basketCheckout.getCity(),
                basketCheckout.getStreet(),
                basketCheckout.getState(),
                basketCheckout.getCountry(),
                basketCheckout.getZipCode(),
                basketCheckout.getCardNumber(),
                basketCheckout.getCardHolderName(),
                basketCheckout.getCardExpiration(),
                basketCheckout.getCardSecurityNumber(),
                basketCheckout.getCardTypeId(),
                basketCheckout.getBuyer(),
                basketCheckout.getRequestId(),
                basket);

        try {
            eventBus.publish(eventMessage);
        } catch (Exception ex) {
            log.error("ERROR Publishing integration event: {} from basket-service",
                    eventMessage.getId(), ex);
            throw ex;
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * Deletes the basket for the given {@code id}.
     * Returns {@code 200 OK} (void — Spring MVC default).
     */
    @DeleteMapping("/{id}")
    public void deleteBasketById(@PathVariable String id) {
        basketRepository.deleteBasket(id);
    }
}
