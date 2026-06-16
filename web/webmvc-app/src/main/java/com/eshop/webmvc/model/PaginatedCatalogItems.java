package com.eshop.webmvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PaginatedCatalogItems {
    private int count;
    private int pageIndex;
    private int pageSize;
    private List<CatalogItem> data;
}
