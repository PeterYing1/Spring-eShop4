package com.eshop.webmvc.web.controllers;

import com.eshop.webmvc.config.AuthHelper;
import com.eshop.webmvc.model.CatalogBrand;
import com.eshop.webmvc.model.CatalogType;
import com.eshop.webmvc.model.CustomerBasket;
import com.eshop.webmvc.model.PaginatedCatalogItems;
import com.eshop.webmvc.services.BasketService;
import com.eshop.webmvc.services.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final BasketService basketService;
    private final AuthHelper authHelper;

    @GetMapping({"", "/", "/catalog"})
    public String catalog(
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(required = false) Integer brandFilterApplied,
            @RequestParam(required = false) Integer typeFilterApplied,
            Authentication auth,
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

        if (auth != null && auth.isAuthenticated()) {
            try {
                String userId = authHelper.getUserId(auth);
                String accessToken = authHelper.getAccessToken(auth);
                CustomerBasket basket = basketService.getBasket(userId, accessToken);
                BigDecimal basketTotal = basket.getItems().stream()
                        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("basket", basket);
                model.addAttribute("basketTotal", basketTotal);
            } catch (Exception e) {
                log.warn("Could not load basket for catalog page: {}", e.getMessage());
                model.addAttribute("basket", new CustomerBasket());
                model.addAttribute("basketTotal", BigDecimal.ZERO);
            }
        }

        return "catalog/index";
    }
}
