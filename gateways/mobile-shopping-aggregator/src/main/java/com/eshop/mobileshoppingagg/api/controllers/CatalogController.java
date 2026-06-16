package com.eshop.mobileshoppingagg.api.controllers;

import com.eshop.mobileshoppingagg.model.CatalogItem;
import com.eshop.mobileshoppingagg.model.PaginatedCatalogItems;
import com.eshop.mobileshoppingagg.services.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BFF catalog controller — proxies catalog-service endpoints to mobile clients.
 *
 * <p>All endpoints are public (no authentication required); the catalog is
 * read-only and contains no user data.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    /**
     * GET /api/v1/catalog/items?pageSize=10&pageIndex=0
     *
     * <p>Returns a paginated list of catalog items.
     */
    @GetMapping("/items")
    public ResponseEntity<PaginatedCatalogItems> getItems(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex) {

        log.debug("GET catalog items pageSize={} pageIndex={}", pageSize, pageIndex);
        PaginatedCatalogItems result = catalogService.getItems(pageSize, pageIndex);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/catalog/items/{id}
     *
     * <p>Returns a single catalog item.
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<CatalogItem> getItemById(@PathVariable int id) {
        log.debug("GET catalog item id={}", id);
        CatalogItem item = catalogService.getItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    /**
     * GET /api/v1/catalog/items/withseeds?ids=1,2,3
     *
     * <p>Returns catalog items for the supplied comma-separated id list.
     */
    @GetMapping("/items/withseeds")
    public ResponseEntity<List<CatalogItem>> getItemsByIds(@RequestParam String ids) {
        log.debug("GET catalog items withseeds ids={}", ids);
        List<CatalogItem> items = catalogService.getItemsByIds(ids);
        return ResponseEntity.ok(items);
    }

    /**
     * GET /api/v1/catalog/catalogbrands
     *
     * <p>Returns all catalog brands (forwarded verbatim from the catalog-service).
     */
    @GetMapping("/catalogbrands")
    public ResponseEntity<Object> getCatalogBrands() {
        log.debug("GET catalog brands");
        return ResponseEntity.ok(catalogService.getCatalogBrands());
    }

    /**
     * GET /api/v1/catalog/catalogtypes
     *
     * <p>Returns all catalog types (forwarded verbatim from the catalog-service).
     */
    @GetMapping("/catalogtypes")
    public ResponseEntity<Object> getCatalogTypes() {
        log.debug("GET catalog types");
        return ResponseEntity.ok(catalogService.getCatalogTypes());
    }
}
