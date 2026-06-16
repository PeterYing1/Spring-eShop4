package com.eshop.catalog.infrastructure;

import com.eshop.catalog.domain.CatalogBrand;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link CatalogBrand} lookup entities.
 */
public interface CatalogBrandRepository extends JpaRepository<CatalogBrand, Integer> {
}
