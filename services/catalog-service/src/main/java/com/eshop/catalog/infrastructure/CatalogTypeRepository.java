package com.eshop.catalog.infrastructure;

import com.eshop.catalog.domain.CatalogType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link CatalogType} lookup entities.
 */
public interface CatalogTypeRepository extends JpaRepository<CatalogType, Integer> {
}
