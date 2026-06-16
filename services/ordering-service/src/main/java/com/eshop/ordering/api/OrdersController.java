package com.eshop.ordering.api;

import com.eshop.ordering.application.commands.CancelOrderCommand;
import com.eshop.ordering.application.commands.CreateOrderDraftCommand;
import com.eshop.ordering.application.commands.ShipOrderCommand;
import com.eshop.ordering.application.commands.handlers.CancelOrderCommandHandler;
import com.eshop.ordering.application.commands.handlers.CreateOrderDraftCommandHandler;
import com.eshop.ordering.application.commands.handlers.IdentifiedCommandHandler;
import com.eshop.ordering.application.commands.handlers.ShipOrderCommandHandler;
import com.eshop.ordering.application.queries.CardType;
import com.eshop.ordering.application.queries.IOrderQueries;
import com.eshop.ordering.application.queries.OrderDraftDTO;
import com.eshop.ordering.application.queries.OrderSummary;
import com.eshop.ordering.application.queries.OrderViewModel;
import com.eshop.security.IIdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST controller for order management.
 *
 * <p>All endpoints require an authenticated user (JWT bearer token).
 * Write commands accept an idempotency key via the {@code x-requestid} header.
 */
@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("isAuthenticated()")
public class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

    private final IOrderQueries orderQueries;
    private final IIdentityService identityService;
    private final CancelOrderCommandHandler cancelOrderCommandHandler;
    private final ShipOrderCommandHandler shipOrderCommandHandler;
    private final CreateOrderDraftCommandHandler createOrderDraftCommandHandler;
    private final IdentifiedCommandHandler identifiedCommandHandler;

    public OrdersController(
            IOrderQueries orderQueries,
            IIdentityService identityService,
            CancelOrderCommandHandler cancelOrderCommandHandler,
            ShipOrderCommandHandler shipOrderCommandHandler,
            CreateOrderDraftCommandHandler createOrderDraftCommandHandler,
            IdentifiedCommandHandler identifiedCommandHandler) {
        this.orderQueries = orderQueries;
        this.identityService = identityService;
        this.cancelOrderCommandHandler = cancelOrderCommandHandler;
        this.shipOrderCommandHandler = shipOrderCommandHandler;
        this.createOrderDraftCommandHandler = createOrderDraftCommandHandler;
        this.identifiedCommandHandler = identifiedCommandHandler;
    }

    // -------------------------------------------------------------------------
    // Write endpoints
    // -------------------------------------------------------------------------

    /**
     * {@code PUT /api/v1/orders/cancel}
     *
     * <p>Cancels the order identified by the command's order number.
     * Requires a valid UUID in the {@code x-requestid} header for idempotency.
     */
    @PutMapping("/cancel")
    public ResponseEntity<Void> cancelOrder(
            @RequestBody CancelOrderCommand command,
            @RequestHeader("x-requestid") String requestId) {

        if (!isValidGuid(requestId)) {
            log.warn("Invalid or missing x-requestid header: '{}'", requestId);
            return ResponseEntity.badRequest().build();
        }

        UUID requestGuid = UUID.fromString(requestId);
        log.info("----- Sending command CancelOrderCommand - orderNumber: {} (requestId={})",
                command.getOrderNumber(), requestGuid);

        boolean result = identifiedCommandHandler.execute(
                requestGuid, "CancelOrderCommand",
                () -> cancelOrderCommandHandler.handle(command),
                false);

        return result ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * {@code PUT /api/v1/orders/ship}
     *
     * <p>Ships the order identified by the command's order number.
     */
    @PutMapping("/ship")
    public ResponseEntity<Void> shipOrder(
            @RequestBody ShipOrderCommand command,
            @RequestHeader("x-requestid") String requestId) {

        if (!isValidGuid(requestId)) {
            log.warn("Invalid or missing x-requestid header: '{}'", requestId);
            return ResponseEntity.badRequest().build();
        }

        UUID requestGuid = UUID.fromString(requestId);
        log.info("----- Sending command ShipOrderCommand - orderNumber: {} (requestId={})",
                command.getOrderNumber(), requestGuid);

        boolean result = identifiedCommandHandler.execute(
                requestGuid, "ShipOrderCommand",
                () -> shipOrderCommandHandler.handle(command),
                false);

        return result ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    // -------------------------------------------------------------------------
    // Read endpoints
    // -------------------------------------------------------------------------

    /**
     * {@code GET /api/v1/orders/{orderId}}
     *
     * <p>Returns the full detail view of a single order.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderViewModel> getOrder(@PathVariable int orderId) {
        try {
            OrderViewModel order = orderQueries.getOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            log.warn("Order #{} not found", orderId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * {@code GET /api/v1/orders}
     *
     * <p>Returns all orders for the currently authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<OrderSummary>> getOrders() {
        String userId = identityService.getUserIdentity();
        log.debug("Fetching orders for user '{}'", userId);
        List<OrderSummary> orders = orderQueries.getOrdersFromUser(UUID.fromString(userId));
        return ResponseEntity.ok(orders);
    }

    /**
     * {@code GET /api/v1/orders/cardtypes}
     *
     * <p>Returns the list of supported card types.
     */
    @GetMapping("/cardtypes")
    public ResponseEntity<List<CardType>> getCardTypes() {
        return ResponseEntity.ok(orderQueries.getCardTypes());
    }

    /**
     * {@code POST /api/v1/orders/draft}
     *
     * <p>Creates a draft order from basket items (not persisted) and returns
     * the computed totals.
     */
    @PostMapping("/draft")
    public ResponseEntity<OrderDraftDTO> createOrderDraft(
            @RequestBody CreateOrderDraftCommand command) {
        log.info("----- Creating order draft for buyer '{}'", command.getBuyerId());
        OrderDraftDTO draft = createOrderDraftCommandHandler.handle(command);
        return ResponseEntity.ok(draft);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isValidGuid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID guid = UUID.fromString(value);
            return !guid.equals(new UUID(0, 0));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
