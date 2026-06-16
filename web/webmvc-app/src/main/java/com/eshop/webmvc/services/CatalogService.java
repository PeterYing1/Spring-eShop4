package com.eshop.webmvc.services;

import com.eshop.webmvc.model.CatalogBrand;
import com.eshop.webmvc.model.CatalogType;
import com.eshop.webmvc.model.PaginatedCatalogItems;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final WebClient catalogClient;

    public PaginatedCatalogItems getItems(int pageSize, int pageIndex, Integer brandId, Integer typeId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/v1/catalog/items")
                .queryParam("pageSize", pageSize)
                .queryParam("pageIndex", pageIndex);
        if (brandId != null) {
            builder.queryParam("catalogBrandId", brandId);
        }
        if (typeId != null) {
            builder.queryParam("catalogTypeId", typeId);
        }
        String uri = builder.toUriString();
        log.debug("Fetching catalog items from: {}", uri);
        return catalogClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(PaginatedCatalogItems.class)
                .block();
    }

    public List<CatalogBrand> getBrands() {
        log.debug("Fetching catalog brands");
        return catalogClient.get()
                .uri("/api/v1/catalog/catalogbrands")
                .retrieve()
                .bodyToFlux(CatalogBrand.class)
                .collectList()
                .block();
    }

    public List<CatalogType> getTypes() {
        log.debug("Fetching catalog types");
        return catalogClient.get()
                .uri("/api/v1/catalog/catalogtypes")
                .retrieve()
                .bodyToFlux(CatalogType.class)
                .collectList()
                .block();
    }
}
