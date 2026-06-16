package com.eshop.ordering.domain.aggregatesmodel.buyer;

import com.eshop.ordering.domain.seedwork.IRepository;

/**
 * Repository contract for the {@link Buyer} aggregate root.
 */
public interface IBuyerRepository extends IRepository<Buyer> {

    /**
     * Persists a new {@link Buyer} and returns the managed instance.
     *
     * @param buyer the buyer to add
     * @return the managed buyer
     */
    Buyer add(Buyer buyer);

    /**
     * Finds a buyer by their identity GUID (the OAuth subject claim).
     *
     * @param identityGuid the identity subject string
     * @return the matching buyer, or {@code null} if none
     */
    Buyer findByIdentityGuid(String identityGuid);

    /**
     * Merges an updated buyer back to the persistence store.
     *
     * @param buyer the buyer to update
     * @return the managed buyer after merge
     */
    Buyer update(Buyer buyer);
}
