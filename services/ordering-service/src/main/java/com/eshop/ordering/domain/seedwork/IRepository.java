package com.eshop.ordering.domain.seedwork;

/**
 * Generic repository abstraction for aggregate roots.
 *
 * @param <T> the type of aggregate root this repository manages
 */
public interface IRepository<T extends IAggregateRoot> {
}
