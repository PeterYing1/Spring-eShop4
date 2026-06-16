package com.eshop.catalog.api;

import com.eshop.catalog.application.CatalogSettings;
import com.eshop.catalog.domain.CatalogBrand;
import com.eshop.catalog.domain.CatalogItem;
import com.eshop.catalog.domain.CatalogType;
import com.eshop.catalog.infrastructure.CatalogBrandRepository;
import com.eshop.catalog.infrastructure.CatalogItemRepository;
import com.eshop.catalog.infrastructure.CatalogTypeRepository;
import com.eshop.catalog.integrationevents.CatalogIntegrationEventService;
import com.eshop.catalog.integrationevents.events.ProductPriceChangedIntegrationEvent;
import com.eshop.websupport.PaginatedItemsViewModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for the Catalog API.
 *
 * <p>Base path: {@code /api/v1/catalog}
 *
 * <p>All list/paginated endpoints call {@code fillProductUrl} on every item
 * to populate the transient {@code pictureUri} field before returning the response.
 */
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogItemRepository catalogItemRepository;
    private final CatalogBrandRepository brandRepository;
    private final CatalogTypeRepository typeRepository;
    private final CatalogIntegrationEventService catalogIntegrationEventService;
    private final CatalogSettings catalogSettings;

    // -------------------------------------------------------------------------
    // GET /items
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of catalog items ordered by name,
     * OR a flat list when comma-separated {@code ids} are provided.
     *
     * <p>When {@code ids} is present:
     * <ul>
     *   <li>Returns {@code 400} if any id is not a valid integer or the list is empty.</li>
     *   <li>Returns {@code 200} with a {@link List} of matching items.</li>
     * </ul>
     *
     * <p>When {@code ids} is absent:
     * <ul>
     *   <li>Returns {@code 200} with a {@link PaginatedItemsViewModel}.</li>
     * </ul>
     */
    @GetMapping("/items")
    public ResponseEntity<?> items(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(required = false) String ids) {

        if (ids != null && !ids.isBlank()) {
            return getItemsByIds(ids);
        }

        Page<CatalogItem> page = catalogItemRepository.findAllByOrderByName(
                PageRequest.of(pageIndex, pageSize));

        List<CatalogItem> items = page.getContent();
        fillProductUrls(items);

        return ResponseEntity.ok(
                new PaginatedItemsViewModel<>(pageIndex, pageSize, page.getTotalElements(), items));
    }

    private ResponseEntity<?> getItemsByIds(String ids) {
        String[] parts = ids.split(",");
        List<Integer> intIds;
        try {
            intIds = Arrays.stream(parts)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest()
                    .body("ids value invalid. Must be comma-separated list of numbers");
        }

        List<CatalogItem> items = catalogItemRepository.findByIdIn(intIds);
        if (items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("ids value invalid. Must be comma-separated list of numbers");
        }

        fillProductUrls(items);
        return ResponseEntity.ok(items);
    }

    // -------------------------------------------------------------------------
    // GET /items/{id}
    // -------------------------------------------------------------------------

    /**
     * Returns a single catalog item by id.
     *
     * <p>Returns {@code 400} if id &le; 0, {@code 404} if not found.
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<CatalogItem> itemById(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Optional<CatalogItem> itemOpt = catalogItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CatalogItem item = itemOpt.get();
        item.fillProductUrl(catalogSettings.getPicBaseUrl(), catalogSettings.isAzureStorageEnabled());
        return ResponseEntity.ok(item);
    }

    // -------------------------------------------------------------------------
    // GET /items/withname/{name}
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of items whose name starts with {@code name}
     * (case-insensitive).
     */
    @GetMapping("/items/withname/{name}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsWithName(
            @PathVariable String name,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex) {

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Page<CatalogItem> page = catalogItemRepository.findByNameStartingWithIgnoreCase(
                name, PageRequest.of(pageIndex, pageSize));

        List<CatalogItem> items = page.getContent();
        fillProductUrls(items);

        return ResponseEntity.ok(
                new PaginatedItemsViewModel<>(pageIndex, pageSize, page.getTotalElements(), items));
    }

    // -------------------------------------------------------------------------
    // GET /items/type/{catalogTypeId}/brand/{catalogBrandId}
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of items filtered by type and optionally by brand.
     */
    @GetMapping("/items/type/{catalogTypeId}/brand/{catalogBrandId}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsByTypeAndBrand(
            @PathVariable int catalogTypeId,
            @PathVariable(required = false) Integer catalogBrandId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex) {

        Page<CatalogItem> page;
        if (catalogBrandId != null) {
            page = catalogItemRepository.findByCatalogTypeIdAndCatalogBrandId(
                    catalogTypeId, catalogBrandId, PageRequest.of(pageIndex, pageSize));
        } else {
            page = catalogItemRepository.findByCatalogTypeId(
                    catalogTypeId, PageRequest.of(pageIndex, pageSize));
        }

        List<CatalogItem> items = page.getContent();
        fillProductUrls(items);

        return ResponseEntity.ok(
                new PaginatedItemsViewModel<>(pageIndex, pageSize, page.getTotalElements(), items));
    }

    // -------------------------------------------------------------------------
    // GET /items/type/all/brand/{catalogBrandId}
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of all items, optionally filtered by brand.
     */
    @GetMapping("/items/type/all/brand/{catalogBrandId}")
    public ResponseEntity<PaginatedItemsViewModel<CatalogItem>> itemsByBrand(
            @PathVariable(required = false) Integer catalogBrandId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex) {

        Page<CatalogItem> page;
        if (catalogBrandId != null) {
            page = catalogItemRepository.findByCatalogBrandId(
                    catalogBrandId, PageRequest.of(pageIndex, pageSize));
        } else {
            page = catalogItemRepository.findAllByOrderByName(PageRequest.of(pageIndex, pageSize));
        }

        List<CatalogItem> items = page.getContent();
        fillProductUrls(items);

        return ResponseEntity.ok(
                new PaginatedItemsViewModel<>(pageIndex, pageSize, page.getTotalElements(), items));
    }

    // -------------------------------------------------------------------------
    // GET /catalogtypes
    // -------------------------------------------------------------------------

    @GetMapping("/catalogtypes")
    public List<CatalogType> catalogTypes() {
        return typeRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // GET /catalogbrands
    // -------------------------------------------------------------------------

    @GetMapping("/catalogbrands")
    public List<CatalogBrand> catalogBrands() {
        return brandRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // PUT /items  — update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing catalog item.
     *
     * <p>If the price changed, a {@link ProductPriceChangedIntegrationEvent} is
     * published via the outbox so downstream consumers (Basket, Webhooks) can
     * react. Otherwise only the database row is updated.
     *
     * <p>Returns {@code 404} if the item does not exist, {@code 201 Created} with
     * a {@code Location} header on success.
     */
    @PutMapping("/items")
    @Transactional
    public ResponseEntity<Void> updateItem(@RequestBody CatalogItem productToUpdate) {
        Optional<CatalogItem> existingOpt = catalogItemRepository.findById(productToUpdate.getId());
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CatalogItem existingItem = existingOpt.get();
        // Capture the old price BEFORE mutating the entity
        java.math.BigDecimal oldPrice = existingItem.getPrice();
        boolean priceChanged = oldPrice.compareTo(productToUpdate.getPrice()) != 0;

        // Copy fields from the incoming request onto the managed entity
        existingItem.setName(productToUpdate.getName());
        existingItem.setDescription(productToUpdate.getDescription());
        existingItem.setPrice(productToUpdate.getPrice());
        existingItem.setPictureFileName(productToUpdate.getPictureFileName());
        existingItem.setCatalogTypeId(productToUpdate.getCatalogTypeId());
        existingItem.setCatalogBrandId(productToUpdate.getCatalogBrandId());
        existingItem.setAvailableStock(productToUpdate.getAvailableStock());
        existingItem.setRestockThreshold(productToUpdate.getRestockThreshold());
        existingItem.setMaxStockThreshold(productToUpdate.getMaxStockThreshold());
        existingItem.setOnReorder(productToUpdate.isOnReorder());

        if (priceChanged) {
            log.info("Price changed for catalog item {} from {} to {} — publishing event",
                    existingItem.getId(), oldPrice, productToUpdate.getPrice());

            var priceChangedEvent = new ProductPriceChangedIntegrationEvent(
                    existingItem.getId(), productToUpdate.getPrice(), oldPrice);

            // Save domain changes and publish event in same transaction
            catalogItemRepository.save(existingItem);
            catalogIntegrationEventService.saveEventAndCatalogContextChangesAsync(priceChangedEvent);
        } else {
            catalogItemRepository.save(existingItem);
        }

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/catalog/items/{id}")
                .buildAndExpand(existingItem.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // POST /items  — create
    // -------------------------------------------------------------------------

    /**
     * Creates a new catalog item.
     *
     * <p>Returns {@code 201 Created} with a {@code Location} header pointing to
     * the new item's GET endpoint.
     */
    @PostMapping("/items")
    public ResponseEntity<Void> createItem(@RequestBody CatalogItem product) {
        CatalogItem item = new CatalogItem();
        item.setName(product.getName());
        item.setDescription(product.getDescription());
        item.setPrice(product.getPrice());
        item.setPictureFileName(product.getPictureFileName());
        item.setCatalogTypeId(product.getCatalogTypeId());
        item.setCatalogBrandId(product.getCatalogBrandId());
        item.setAvailableStock(product.getAvailableStock());
        item.setRestockThreshold(product.getRestockThreshold());
        item.setMaxStockThreshold(product.getMaxStockThreshold());
        item.setOnReorder(product.isOnReorder());

        CatalogItem saved = catalogItemRepository.save(item);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/catalog/items/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /{id}
    // -------------------------------------------------------------------------

    /**
     * Deletes a catalog item by id.
     *
     * <p>Returns {@code 404} if not found, {@code 204 No Content} on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable int id) {
        Optional<CatalogItem> itemOpt = catalogItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        catalogItemRepository.delete(itemOpt.get());
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void fillProductUrls(List<CatalogItem> items) {
        String baseUrl = catalogSettings.getPicBaseUrl();
        boolean azure = catalogSettings.isAzureStorageEnabled();
        for (CatalogItem item : items) {
            item.fillProductUrl(baseUrl, azure);
        }
    }
}
