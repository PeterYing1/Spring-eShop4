package com.eshop.ordering.infrastructure;

import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.IBuyerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of {@link IBuyerRepository}.
 */
@Repository
@Transactional
public class BuyerRepository implements IBuyerRepository {

    private static final Logger log = LoggerFactory.getLogger(BuyerRepository.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Buyer add(Buyer buyer) {
        log.debug("Persisting new buyer identityGuid={}", buyer.getIdentityGuid());
        em.persist(buyer);
        return buyer;
    }

    @Override
    @Transactional(readOnly = true)
    public Buyer findByIdentityGuid(String identityGuid) {
        log.debug("Finding buyer by identityGuid={}", identityGuid);
        try {
            TypedQuery<Buyer> query = em.createQuery(
                    "SELECT b FROM Buyer b LEFT JOIN FETCH b.paymentMethods WHERE b.identityGuid = :guid",
                    Buyer.class);
            query.setParameter("guid", identityGuid);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Buyer update(Buyer buyer) {
        log.debug("Merging buyer id={}", buyer.getId());
        return em.merge(buyer);
    }
}
