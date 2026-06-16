package com.eshop.ordering.domain.aggregatesmodel.order;

import com.eshop.ordering.domain.seedwork.IRepository;

/**
 * Repository contract for the {@link Order} aggregate root.
 *
 * <p>Follows the DDD repository pattern: the interface lives in the domain
 * layer and the implementation lives in the infrastructure layer.
 */
public interface IOrderRepository extends IRepository<Order> {

    /**
     * Persists a new {@link Order} aggregate and returns the managed instance.
     *
     * @param order the order to add
     * @return the managed (persisted) order
     */
    Order add(Order order);

    /**
     * Loads an existing {@link Order} by its primary key, including all child
     * {@link OrderItem}s.
     *
     * @param orderId the order primary key
     * @return the order, or {@code null} if not found
     */
    Order get(int orderId);

    /**
     * Merges changes to an existing {@link Order} back to the persistence store.
     *
     * @param order the order to update
     */
    void update(Order order);
}
