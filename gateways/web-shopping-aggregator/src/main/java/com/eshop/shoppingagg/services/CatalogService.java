package com.eshop.shoppingagg.services;

import com.eshop.shoppingagg.model.CatalogItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Calls the catalog service to retrieve item details.
 *
 * <p>Catalog endpoints are public — no Bearer token is forwarded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    @Qualifier("catalogServiceClient")
    private final WebClient catalogServiceClient;

    /**
     * Fetches a single catalog item by its ID.
     *
     * @param id the catalog item ID
     * @return the {@link CatalogItem}, or {@code null} if not found (HTTP 404)
     */
    public CatalogItem getItemById(int id) {
        log.debug("Fetching catalog item id={}", id);
        try {
            return catalogServiceClient.get()
                    .uri("/api/v1/catalog/items/{id}", id)
                    .retrieve()
                    .bodyToMono(CatalogItem.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Catalog item not found: id={}", id);
                return null;
            }
            log.error("Error fetching catalog item id={}: {}", id, ex.getMessage());
            throw ex;
        }
    }
}
