package com.eshop.ordering.domain.exceptions;

/**
 * Domain exception for the Ordering bounded context.
 *
 * <p>Thrown when a domain invariant is violated, such as an invalid status
 * transition, negative discount, or insufficient stock.
 */
public class OrderingDomainException extends RuntimeException {

    public OrderingDomainException(String message) {
        super(message);
    }

    public OrderingDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
