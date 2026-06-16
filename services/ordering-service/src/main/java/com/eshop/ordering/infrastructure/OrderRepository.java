package com.eshop.ordering.infrastructure;

import com.eshop.ordering.domain.aggregatesmodel.order.IOrderRepository;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JPA implementation of {@link IOrderRepository}.
 *
 * <p>Uses the {@link EntityManager} directly to align with the DDD aggregate
 * pattern — eager-loading the order items collection ensures the aggregate is
 * always complete.
 */
@Repository
@Transactional
public class OrderRepository implements IOrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Order add(Order order) {
        log.debug("Persisting new order");
        em.persist(order);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Order get(int orderId) {
        log.debug("Loading order id={}", orderId);
        // Use JOIN FETCH to eagerly load order items in one query
        TypedQuery<Order> query = em.createQuery(
                "SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id",
                Order.class);
        query.setParameter("id", orderId);
        List<Order> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public void update(Order order) {
        log.debug("Merging order id={}", order.getId());
        em.merge(order);
    }
}
