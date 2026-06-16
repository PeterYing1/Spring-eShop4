package com.eshop.mobileshoppingagg.services;

import com.eshop.mobileshoppingagg.config.AggregatorConfig;
import com.eshop.mobileshoppingagg.model.CatalogItem;
import com.eshop.mobileshoppingagg.model.PaginatedCatalogItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Calls the catalog-service REST API.
 *
 * <p>Catalog reads are public — no bearer token is forwarded.
 * All calls use {@link WebClient#block()} because the aggregator runs in a
 * servlet (blocking) context.
 */
@Slf4j
@Service
public class CatalogService {

    private final WebClient webClient;

    public CatalogService(WebClient.Builder webClientBuilder, AggregatorConfig config) {
        this.webClient = webClientBuilder
                .baseUrl(config.getCatalogUrl())
                .build();
    }

    /**
     * Returns a paginated list of all catalog items.
     *
     * @param pageSize  number of items per page (default 10)
     * @param pageIndex zero-based page index (default 0)
     */
    public PaginatedCatalogItems getItems(int pageSize, int pageIndex) {
        log.debug("Fetching catalog items pageSize={} pageIndex={}", pageSize, pageIndex);
        return webClient.get()
                .uri("/api/v1/catalog/items?pageSize={pageSize}&pageIndex={pageIndex}", pageSize, pageIndex)
                .retrieve()
                .bodyToMono(PaginatedCatalogItems.class)
                .block();
    }

    /**
     * Returns a single catalog item by its id.
     */
    public CatalogItem getItemById(int id) {
        log.debug("Fetching catalog item id={}", id);
        return webClient.get()
                .uri("/api/v1/catalog/items/{id}", id)
                .retrieve()
                .bodyToMono(CatalogItem.class)
                .block();
    }

    /**
     * Returns catalog items matching the supplied comma-separated id list.
     *
     * @param ids comma-separated catalog item ids (e.g. {@code "1,2,3"})
     */
    public List<CatalogItem> getItemsByIds(String ids) {
        log.debug("Fetching catalog items by ids={}", ids);
        return webClient.get()
                .uri("/api/v1/catalog/items?ids={ids}", ids)
                .retrieve()
                .bodyToFlux(CatalogItem.class)
                .collectList()
                .block();
    }

    /**
     * Returns items filtered by catalog item ids for internal basket enrichment
     * (accepts {@link Iterable} of product ids).
     */
    public List<CatalogItem> getCatalogItemsAsync(Iterable<Integer> ids) {
        String joined = join(ids);
        log.debug("Fetching catalog items by ids={}", joined);
        return webClient.get()
                .uri("/api/v1/catalog/items?ids={ids}", joined)
                .retrieve()
                .bodyToFlux(CatalogItem.class)
                .collectList()
                .block();
    }

    /**
     * Returns a single catalog item used during basket item addition.
     */
    public CatalogItem getCatalogItemAsync(int id) {
        return getItemById(id);
    }

    /**
     * Returns all items for a given brand, paginated.
     *
     * @param brandId   catalog brand id
     * @param pageSize  page size
     * @param pageIndex zero-based page index
     */
    public PaginatedCatalogItems getItemsByBrand(int brandId, int pageSize, int pageIndex) {
        log.debug("Fetching catalog items brandId={} pageSize={} pageIndex={}", brandId, pageSize, pageIndex);
        return webClient.get()
                .uri("/api/v1/catalog/items/type/all/brand/{brandId}?pageSize={pageSize}&pageIndex={pageIndex}",
                        brandId, pageSize, pageIndex)
                .retrieve()
                .bodyToMono(PaginatedCatalogItems.class)
                .block();
    }

    /**
     * Returns all catalog brands as a raw JSON array (forwarded verbatim to the caller).
     */
    public Object getCatalogBrands() {
        log.debug("Fetching catalog brands");
        return webClient.get()
                .uri("/api/v1/catalog/catalogbrands")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Object>() {})
                .block();
    }

    /**
     * Returns all catalog types as a raw JSON array (forwarded verbatim to the caller).
     */
    public Object getCatalogTypes() {
        log.debug("Fetching catalog types");
        return webClient.get()
                .uri("/api/v1/catalog/catalogtypes")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Object>() {})
                .block();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static String join(Iterable<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (Integer id : ids) {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }
}
