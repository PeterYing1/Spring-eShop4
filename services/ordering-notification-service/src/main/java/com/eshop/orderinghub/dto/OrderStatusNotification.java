package com.eshop.orderinghub.dto;

import java.time.Instant;

/**
 * Payload sent to WebSocket clients when an order status changes.
 *
 * <p>Mirrors the anonymous object sent by the .NET hub:
 * {@code new { OrderId = @event.OrderId, Status = @event.OrderStatus }},
 * enriched with {@code buyerName} and {@code updatedAt} for clients that
 * need those fields without a separate HTTP call.
 *
 * @param orderId   the unique identifier of the order
 * @param status    human-readable order status string (e.g. "Awaiting Validation")
 * @param buyerName the username / buyer identity of the order owner
 * @param updatedAt the instant at which the status change event was processed
 */
public record OrderStatusNotification(
        int orderId,
        String status,
        String buyerName,
        Instant updatedAt
) {
}
