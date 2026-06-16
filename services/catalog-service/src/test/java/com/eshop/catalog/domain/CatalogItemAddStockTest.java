package com.eshop.catalog.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CatalogItem#addStock(int)}.
 * No Spring context — pure domain logic tests.
 */
@DisplayName("CatalogItem.addStock")
class CatalogItemAddStockTest {

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    private CatalogItem createCatalogItem(int availableStock, int maxStockThreshold, int restockThreshold) {
        CatalogItem item = new CatalogItem();
        item.setAvailableStock(availableStock);
        item.setMaxStockThreshold(maxStockThreshold);
        item.setRestockThreshold(restockThreshold);
        item.setOnReorder(false);
        item.setName("Test Item");
        item.setPrice(new BigDecimal("10.00"));
        item.setCatalogTypeId(1);
        item.setCatalogBrandId(1);
        return item;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addStock_addsToAvailableStock")
    void addStock_addsToAvailableStock() {
        // Arrange
        CatalogItem item = createCatalogItem(5, 100, 10);

        // Act
        int added = item.addStock(3);

        // Assert
        assertEquals(8, item.getAvailableStock(),
                "Available stock should increase from 5 to 8");
        assertEquals(3, added,
                "Method should return the number of units actually added");
    }

    @Test
    @DisplayName("addStock_capsAtMaxStockThreshold")
    void addStock_capsAtMaxStockThreshold() {
        // Arrange: stock=95, max=100, adding 10 would exceed max
        CatalogItem item = createCatalogItem(95, 100, 10);

        // Act
        int added = item.addStock(10);

        // Assert
        assertEquals(100, item.getAvailableStock(),
                "Available stock should be capped at maxStockThreshold (100)");
        assertEquals(5, added,
                "Only 5 units should actually be added (not the full 10)");
    }

    @Test
    @DisplayName("addStock_clears_onReorder_when_aboveRestockThreshold")
    void addStock_clears_onReorder_when_aboveRestockThreshold() {
        // Arrange: item is on reorder; after adding stock it should no longer be
        CatalogItem item = createCatalogItem(5, 100, 10);
        item.setOnReorder(true);

        // Act: add enough stock to clearly exceed the restockThreshold (10)
        item.addStock(20);

        // Assert
        assertFalse(item.isOnReorder(),
                "onReorder flag should be cleared after successfully adding stock above restock threshold");
        assertEquals(25, item.getAvailableStock(),
                "Available stock should be 5 + 20 = 25");
    }

    @Test
    @DisplayName("addStock_fromZero")
    void addStock_fromZero() {
        // Arrange
        CatalogItem item = createCatalogItem(0, 100, 10);

        // Act
        int added = item.addStock(50);

        // Assert
        assertEquals(50, item.getAvailableStock(),
                "Available stock should be 50 after adding from zero");
        assertEquals(50, added,
                "All 50 units should have been added");
    }

    @Test
    @DisplayName("addStock_returnsQtyOverThreshold_whenExceedingMax")
    void addStock_returnsQtyOverThreshold_whenExceedingMax() {
        // Arrange: stock=90, max=100; adding 20 exceeds max by 10
        CatalogItem item = createCatalogItem(90, 100, 10);

        // Act
        int added = item.addStock(20);

        // Assert: only 10 units can be added to reach the cap
        assertEquals(100, item.getAvailableStock(),
                "Stock should be capped at 100");
        assertEquals(10, added,
                "Return value is the units actually added (100 - 90 = 10), not the requested 20");

        // The overage (units NOT added) is: quantityRequested - added = 20 - 10 = 10
        int overage = 20 - added;
        assertEquals(10, overage,
                "10 units were requested but could not be stored (above max threshold)");
    }

    @Test
    @DisplayName("addStock_alwaysClearsOnReorder_regardlessOfThreshold")
    void addStock_alwaysClearsOnReorder_regardlessOfThreshold() {
        // Arrange: restock threshold is 10, current stock is 5, adding only 1
        // so final stock (6) is still below restockThreshold — but onReorder
        // is always cleared on any addStock call per the implementation contract
        CatalogItem item = createCatalogItem(5, 100, 10);
        item.setOnReorder(true);

        // Act
        item.addStock(1);

        // Assert
        assertFalse(item.isOnReorder(),
                "onReorder is always set to false by addStock, regardless of the restock threshold");
        assertEquals(6, item.getAvailableStock());
    }
}
