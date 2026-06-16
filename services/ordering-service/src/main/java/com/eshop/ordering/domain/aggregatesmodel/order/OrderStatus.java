package com.eshop.ordering.domain.aggregatesmodel.order;

import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import com.eshop.ordering.domain.seedwork.Enumeration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ordering status enumeration — not a JPA entity.
 *
 * <p>The status id is stored directly as an integer FK column
 * ({@code OrderStatusId}) in the {@code ordering.orders} table.
 * The {@code ordering.orderstatus} lookup table is seeded with the same
 * values and exists for reporting queries only.
 */
public class OrderStatus extends Enumeration {

    public static final OrderStatus SUBMITTED           = new OrderStatus(1, "submitted");
    public static final OrderStatus AWAITING_VALIDATION = new OrderStatus(2, "awaitingvalidation");
    public static final OrderStatus STOCK_CONFIRMED     = new OrderStatus(3, "stockconfirmed");
    public static final OrderStatus PAID                = new OrderStatus(4, "paid");
    public static final OrderStatus SHIPPED             = new OrderStatus(5, "shipped");
    public static final OrderStatus CANCELLED           = new OrderStatus(6, "cancelled");

    private OrderStatus(int id, String name) {
        super(id, name);
    }

    // -------------------------------------------------------------------------
    // Factory / look-up
    // -------------------------------------------------------------------------

    /** Returns all known order statuses. */
    public static List<OrderStatus> list() {
        return Enumeration.getAll(OrderStatus.class);
    }

    /**
     * Looks up an {@link OrderStatus} by its name (case-insensitive).
     *
     * @throws OrderingDomainException if not found
     */
    public static OrderStatus fromName(String name) {
        return list().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new OrderingDomainException(
                        "Possible values for OrderStatus: "
                        + list().stream().map(OrderStatus::getName).collect(Collectors.joining(","))));
    }

    /**
     * Looks up an {@link OrderStatus} by its integer id.
     *
     * @throws OrderingDomainException if not found
     */
    public static OrderStatus from(int id) {
        return list().stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElseThrow(() -> new OrderingDomainException(
                        "Possible values for OrderStatus: "
                        + list().stream().map(OrderStatus::getName).collect(Collectors.joining(","))));
    }
}
