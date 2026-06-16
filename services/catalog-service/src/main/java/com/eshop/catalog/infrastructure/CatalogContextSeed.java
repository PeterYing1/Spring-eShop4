package com.eshop.catalog.infrastructure;

import com.eshop.catalog.domain.CatalogBrand;
import com.eshop.catalog.domain.CatalogItem;
import com.eshop.catalog.domain.CatalogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the catalog database with reference brands, types, and 12 catalog items
 * on first startup. Seeding is skipped if any brands already exist.
 *
 * <p>The seed data mirrors the CSV data bundled with the .NET source application
 * ({@code Setup/CatalogItems.csv}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogContextSeed implements ApplicationRunner {

    private final CatalogBrandRepository brandRepository;
    private final CatalogTypeRepository typeRepository;
    private final CatalogItemRepository itemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!brandRepository.findAll().isEmpty()) {
            log.info("Catalog database already seeded — skipping.");
            return;
        }

        log.info("Seeding catalog database...");
        seedBrandsAndTypes();
        log.info("Catalog seed complete.");
    }

    private void seedBrandsAndTypes() {
        // ----- Brands -----
        CatalogBrand azure      = brandRepository.save(new CatalogBrand(null, "Azure"));
        CatalogBrand dotNet     = brandRepository.save(new CatalogBrand(null, ".NET"));
        CatalogBrand visualStudio = brandRepository.save(new CatalogBrand(null, "Visual Studio"));
        CatalogBrand sqlServer  = brandRepository.save(new CatalogBrand(null, "SQL Server"));
        CatalogBrand other      = brandRepository.save(new CatalogBrand(null, "Other"));

        // ----- Types -----
        CatalogType mug      = typeRepository.save(new CatalogType(null, "Mug"));
        CatalogType tShirt   = typeRepository.save(new CatalogType(null, "T-Shirt"));
        CatalogType sheet    = typeRepository.save(new CatalogType(null, "Sheet"));
        CatalogType usbStick = typeRepository.save(new CatalogType(null, "USB Memory Stick"));

        // ----- Catalog Items -----
        List<CatalogItem> items = List.of(
                item(".NET Bot Black Hoodie",          "Hoodie",                    19.5, "1.png", dotNet.getId(),     tShirt.getId(),   100, 0,  200),
                item(".NET Black & White Mug",         "Mug",                        8.5, "2.png", dotNet.getId(),     mug.getId(),      89,  0,  200),
                item("Prism White T-Shirt",            "T-Shirt",                   12.0, "3.png", other.getId(),      tShirt.getId(),   56,  0,  200),
                item(".NET Foundation T-shirt",        "T-Shirt",                   12.0, "4.png", dotNet.getId(),     tShirt.getId(),   120, 0,  200),
                item("Roslyn Red Sheet",               "Sheet",                      8.5, "5.png", other.getId(),      sheet.getId(),    55,  0,  200),
                item(".NET Blue Hoodie",               "Hoodie",                    12.0, "6.png", dotNet.getId(),     tShirt.getId(),   17,  0,  200),
                item("Roslyn Red T-Shirt",             "T-Shirt",                   12.0, "7.png", dotNet.getId(),     tShirt.getId(),   8,   0,  200),
                item("Kudu Purple Hoodie",             "Hoodie",                    8.5,  "8.png", other.getId(),      tShirt.getId(),   34,  0,  200),
                item("Cup<T> White Mug",               "Mug",                       12.0, "9.png", other.getId(),      mug.getId(),      76,  0,  200),
                item(".NET Foundation Sheet",          "Sheet",                     12.0, "10.png", dotNet.getId(),    sheet.getId(),    11,  0,  200),
                item("Cup<T> Sheet",                   "Sheet",                      8.5, "11.png", other.getId(),     sheet.getId(),    3,   0,  200),
                item("Prism White TShirt",             "T-Shirt",                   12.0, "12.png", other.getId(),     tShirt.getId(),   0,   0,  200)
        );

        itemRepository.saveAll(items);
        log.info("Inserted {} catalog items.", items.size());
    }

    private CatalogItem item(String name, String description, double price, String pictureFileName,
                              int brandId, int typeId, int availableStock, int restockThreshold, int maxStockThreshold) {
        CatalogItem item = new CatalogItem();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(BigDecimal.valueOf(price));
        item.setPictureFileName(pictureFileName);
        item.setCatalogBrandId(brandId);
        item.setCatalogTypeId(typeId);
        item.setAvailableStock(availableStock);
        item.setRestockThreshold(restockThreshold);
        item.setMaxStockThreshold(maxStockThreshold);
        item.setOnReorder(false);
        return item;
    }
}
