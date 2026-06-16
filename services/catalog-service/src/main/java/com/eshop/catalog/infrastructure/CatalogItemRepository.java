package com.eshop.catalog.infrastructure;

import com.eshop.catalog.domain.CatalogItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link CatalogItem}.
 *
 * <p>All paged queries accept a {@link Pageable} argument so the controller
 * can drive pagination and sorting without additional service-layer code.
 */
public interface CatalogItemRepository extends JpaRepository<CatalogItem, Integer> {

    /**
     * Returns all catalog items ordered by {@code Name} ascending.
     */
    Page<CatalogItem> findAllByOrderByName(Pageable pageable);

    /**
     * Returns items whose {@code Name} starts with {@code name}
     * (case-insensitive prefix match).
     */
    Page<CatalogItem> findByNameStartingWithIgnoreCase(String name, Pageable pageable);

    /**
     * Filters by catalog type id.
     */
    Page<CatalogItem> findByCatalogTypeId(int catalogTypeId, Pageable pageable);

    /**
     * Filters by catalog brand id.
     */
    Page<CatalogItem> findByCatalogBrandId(int catalogBrandId, Pageable pageable);

    /**
     * Filters by both catalog type id and catalog brand id.
     */
    Page<CatalogItem> findByCatalogTypeIdAndCatalogBrandId(int catalogTypeId, int catalogBrandId, Pageable pageable);

    /**
     * Loads multiple items by their ids (used for the comma-separated {@code ids}
     * query parameter on {@code GET /api/v1/catalog/items}).
     */
    List<CatalogItem> findByIdIn(List<Integer> ids);
}
