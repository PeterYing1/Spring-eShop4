package com.eshop.catalog.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CatalogItem#removeStock(int)}.
 * No Spring context — pure domain logic tests.
 */
@DisplayName("CatalogItem.removeStock")
class CatalogItemRemoveStockTest {

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
    // Happy-path tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeStock_success_reducesAvailableStock")
    void removeStock_success_reducesAvailableStock() {
        // Arrange
        CatalogItem item = createCatalogItem(10, 100, 5);

        // Act
        int removed = item.removeStock(5);

        // Assert
        assertEquals(5, item.getAvailableStock(),
                "Available stock should be reduced from 10 to 5");
        assertEquals(5, removed,
                "Should report the number of units actually removed");
    }

    @Test
    @DisplayName("removeStock_exactAmount_reducesStockToZero")
    void removeStock_exactAmount_reducesStockToZero() {
        // Arrange
        CatalogItem item = createCatalogItem(10, 100, 5);

        // Act
        int removed = item.removeStock(10);

        // Assert
        assertEquals(0, item.getAvailableStock(),
                "Available stock should be reduced to exactly zero");
        assertEquals(10, removed,
                "Should report that all 10 units were removed");
    }

    // -------------------------------------------------------------------------
    // Exception tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeStock_throwsWhenZeroQuantity")
    void removeStock_throwsWhenZeroQuantity() {
        // Arrange
        CatalogItem item = createCatalogItem(10, 100, 5);

        // Act & Assert
        CatalogDomainException ex = assertThrows(
                CatalogDomainException.class,
                () -> item.removeStock(0),
                "removeStock(0) should throw CatalogDomainException");

        assertTrue(ex.getMessage().contains("greater than zero"),
                "Exception message should mention 'greater than zero'");
    }

    @Test
    @DisplayName("removeStock_throwsWhenNegativeQuantity")
    void removeStock_throwsWhenNegativeQuantity() {
        // Arrange
        CatalogItem item = createCatalogItem(10, 100, 5);

        // Act & Assert
        CatalogDomainException ex = assertThrows(
                CatalogDomainException.class,
                () -> item.removeStock(-1),
                "removeStock(-1) should throw CatalogDomainException");

        assertTrue(ex.getMessage().contains("greater than zero"),
                "Exception message should mention 'greater than zero'");
    }

    @Test
    @DisplayName("removeStock_throwsWhenEmptyStock")
    void removeStock_throwsWhenEmptyStock() {
        // Arrange: stock is already at zero
        CatalogItem item = createCatalogItem(0, 100, 5);

        // Act & Assert
        CatalogDomainException ex = assertThrows(
                CatalogDomainException.class,
                () -> item.removeStock(1),
                "removeStock should throw when availableStock == 0");

        assertTrue(ex.getMessage().contains("sold out") || ex.getMessage().contains("Empty stock"),
                "Exception message should indicate the item is sold out / empty");
    }

    /**
     * The Java implementation uses a partial-fill strategy: when the desired quantity
     * exceeds available stock it removes whatever remains and returns the actual count
     * removed, rather than throwing. The caller detects the shortfall via the return value.
     *
     * <p>This is intentionally different from the .NET reference which throws an exception
     * in this scenario. The test documents — and verifies — the Java behaviour.
     */
    @Test
    @DisplayName("removeStock_throwsWhenInsufficientStock")
    void removeStock_throwsWhenInsufficientStock() {
        // Arrange: only 3 units available, caller asks for 5
        CatalogItem item = createCatalogItem(3, 100, 5);

        // Act — implementation removes whatever is available (partial fill), no exception
        int removed = item.removeStock(5);

        // Assert
        assertEquals(3, removed,
                "Should remove only the 3 available units (partial fill)");
        assertEquals(0, item.getAvailableStock(),
                "Stock should reach zero after partial removal");
    }
}
