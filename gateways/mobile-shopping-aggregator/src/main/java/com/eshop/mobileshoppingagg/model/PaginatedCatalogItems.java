package com.eshop.mobileshoppingagg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated wrapper returned by the catalog-service for list endpoints.
 * Matches the {@code PaginatedItemsViewModel<T>} shape: {@code {count, pageIndex, pageSize, data}}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedCatalogItems {

    private int count;
    private int pageIndex;
    private int pageSize;
    private List<CatalogItem> data;
}
