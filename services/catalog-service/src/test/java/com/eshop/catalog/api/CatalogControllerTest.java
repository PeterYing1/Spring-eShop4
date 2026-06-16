package com.eshop.catalog.api;

import com.eshop.catalog.application.CatalogSettings;
import com.eshop.catalog.domain.CatalogBrand;
import com.eshop.catalog.domain.CatalogItem;
import com.eshop.catalog.domain.CatalogType;
import com.eshop.catalog.infrastructure.CatalogBrandRepository;
import com.eshop.catalog.infrastructure.CatalogItemRepository;
import com.eshop.catalog.infrastructure.CatalogTypeRepository;
import com.eshop.catalog.integrationevents.CatalogIntegrationEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link CatalogController} using {@code @WebMvcTest}.
 *
 * <p>Only the MVC layer is loaded — repositories and services are replaced with
 * Mockito mocks.  The security filter chain is disabled via
 * {@code @AutoConfigureMockMvc(addFilters = false)} so that tests focus purely
 * on controller behaviour.
 */
@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CatalogController — MVC slice tests")
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogItemRepository catalogItemRepository;

    @MockBean
    private CatalogBrandRepository brandRepository;

    @MockBean
    private CatalogTypeRepository typeRepository;

    @MockBean
    private CatalogIntegrationEventService catalogIntegrationEventService;

    @MockBean
    private CatalogSettings catalogSettings;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CatalogItem buildItem(int id, String name, BigDecimal price) {
        CatalogItem item = new CatalogItem();
        item.setName(name);
        item.setPrice(price);
        item.setCatalogTypeId(1);
        item.setCatalogBrandId(1);
        item.setAvailableStock(10);
        item.setMaxStockThreshold(100);
        item.setRestockThreshold(5);
        item.setOnReorder(false);
        // Reflectively set the id so the controller can build the Location header
        try {
            var field = CatalogItem.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(item, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/catalog/items
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getItems_returnsPage")
    void getItems_returnsPage() throws Exception {
        // Arrange
        CatalogItem item = buildItem(1, "Widget", new BigDecimal("9.99"));
        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

        when(catalogSettings.getPicBaseUrl()).thenReturn("http://localhost/pic/[0]");
        when(catalogSettings.isAzureStorageEnabled()).thenReturn(false);
        when(catalogItemRepository.findAllByOrderByName(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/catalog/items")
                        .param("pageSize", "10")
                        .param("pageIndex", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pageIndex").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Widget"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/catalog/items/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getItemById_returnsItem")
    void getItemById_returnsItem() throws Exception {
        // Arrange
        CatalogItem item = buildItem(1, "Widget", new BigDecimal("9.99"));

        when(catalogSettings.getPicBaseUrl()).thenReturn("http://localhost/pic/[0]");
        when(catalogSettings.isAzureStorageEnabled()).thenReturn(false);
        when(catalogItemRepository.findById(1)).thenReturn(Optional.of(item));

        // Act & Assert
        mockMvc.perform(get("/api/v1/catalog/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    @DisplayName("getItemById_notFound_returns404")
    void getItemById_notFound_returns404() throws Exception {
        // Arrange
        when(catalogItemRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/catalog/items/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getItemById_invalidId_returns400")
    void getItemById_invalidId_returns400() throws Exception {
        // id <= 0 is rejected by the controller before hitting the repository
        mockMvc.perform(get("/api/v1/catalog/items/0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/catalog/catalogbrands
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCatalogBrands_returnsList")
    void getCatalogBrands_returnsList() throws Exception {
        // Arrange
        List<CatalogBrand> brands = List.of(
                new CatalogBrand(1, "Azure"),
                new CatalogBrand(2, "Red Hat")
        );
        when(brandRepository.findAll()).thenReturn(brands);

        // Act & Assert
        mockMvc.perform(get("/api/v1/catalog/catalogbrands")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].brand").value("Azure"))
                .andExpect(jsonPath("$[1].brand").value("Red Hat"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/catalog/catalogtypes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCatalogTypes_returnsList")
    void getCatalogTypes_returnsList() throws Exception {
        // Arrange
        List<CatalogType> types = List.of(
                new CatalogType(1, "Mug"),
                new CatalogType(2, "T-Shirt")
        );
        when(typeRepository.findAll()).thenReturn(types);

        // Act & Assert
        mockMvc.perform(get("/api/v1/catalog/catalogtypes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("Mug"))
                .andExpect(jsonPath("$[1].type").value("T-Shirt"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/catalog/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteItem_notFound_returns404")
    void deleteItem_notFound_returns404() throws Exception {
        // Arrange
        when(catalogItemRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/catalog/999"))
                .andExpect(status().isNotFound());
    }
}
