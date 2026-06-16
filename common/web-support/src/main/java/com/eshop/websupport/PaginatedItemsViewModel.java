package com.eshop.websupport;

import java.util.List;

/**
 * Generic paginated response wrapper that matches the .NET source application's
 * {@code PaginatedItemsViewModel<T>} contract exactly.
 *
 * <p>JSON shape:
 * <pre>{@code
 * {
 *   "pageIndex": 0,
 *   "pageSize": 10,
 *   "count": 42,
 *   "data": [ ... ]
 * }
 * }</pre>
 *
 * @param <T>       the item type
 * @param pageIndex zero-based page index
 * @param pageSize  number of items per page
 * @param count     total number of items across all pages
 * @param data      items on the current page
 */
public record PaginatedItemsViewModel<T>(
        int pageIndex,
        int pageSize,
        long count,
        List<T> data) {

    /**
     * Convenience factory.
     *
     * @param <T>       item type
     * @param pageIndex zero-based page index
     * @param pageSize  items per page
     * @param count     total item count
     * @param data      current page items
     * @return a new {@link PaginatedItemsViewModel}
     */
    public static <T> PaginatedItemsViewModel<T> of(
            int pageIndex, int pageSize, long count, List<T> data) {
        return new PaginatedItemsViewModel<>(pageIndex, pageSize, count, data);
    }
}
