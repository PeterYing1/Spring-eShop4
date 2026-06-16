package com.eshop.webmvc.web.controllers;

import com.eshop.webmvc.model.CatalogBrand;
import com.eshop.webmvc.model.CatalogType;
import com.eshop.webmvc.model.PaginatedCatalogItems;
import com.eshop.webmvc.services.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping({"", "/", "/catalog"})
    public String catalog(
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(required = false) Integer brandFilterApplied,
            @RequestParam(required = false) Integer typeFilterApplied,
            Model model) {

        log.debug("Catalog page: pageIndex={}, pageSize={}, brand={}, type={}",
                pageIndex, pageSize, brandFilterApplied, typeFilterApplied);

        PaginatedCatalogItems catalog = catalogService.getItems(pageSize, pageIndex, brandFilterApplied, typeFilterApplied);
        List<CatalogBrand> brands = catalogService.getBrands();
        List<CatalogType> types = catalogService.getTypes();

        int totalPages = (catalog.getCount() == 0) ? 1
                : (int) Math.ceil((double) catalog.getCount() / pageSize);

        model.addAttribute("catalogItems", catalog.getData());
        model.addAttribute("brands", brands);
        model.addAttribute("types", types);
        model.addAttribute("brandFilterApplied", brandFilterApplied != null ? brandFilterApplied : 0);
        model.addAttribute("typeFilterApplied", typeFilterApplied != null ? typeFilterApplied : 0);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalItems", catalog.getCount());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", pageIndex > 0);
        model.addAttribute("hasNext", pageIndex < totalPages - 1);

        return "catalog/index";
    }
}
