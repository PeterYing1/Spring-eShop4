package com.eshop.catalog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Catalog product entity. Maps to the {@code Catalog} table.
 *
 * <p>Business rules:
 * <ul>
 *   <li>{@link #removeStock(int)} decrements available stock; throws if stock is
 *       empty or quantity is non-positive.</li>
 *   <li>{@link #addStock(int)} increments stock up to {@code maxStockThreshold}
 *       and clears the {@code onReorder} flag.</li>
 *   <li>{@link #fillProductUrl(String, boolean)} populates the transient
 *       {@code pictureUri} field at read time.</li>
 * </ul>
 */
@Entity
@Table(name = "Catalog")
@Getter
@Setter
@NoArgsConstructor
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "Price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "PictureFileName", length = 255)
    private String pictureFileName;

    /**
     * Picture URI built at read time from the configured base URL.
     * Not persisted — annotated {@code @Transient}.
     */
    @Transient
    private String pictureUri;

    @Column(name = "CatalogTypeId", nullable = false)
    private Integer catalogTypeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CatalogTypeId", insertable = false, updatable = false)
    private CatalogType catalogType;

    @Column(name = "CatalogBrandId", nullable = false)
    private Integer catalogBrandId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CatalogBrandId", insertable = false, updatable = false)
    private CatalogBrand catalogBrand;

    @Column(name = "AvailableStock", nullable = false)
    private int availableStock;

    @Column(name = "RestockThreshold", nullable = false)
    private int restockThreshold;

    @Column(name = "MaxStockThreshold", nullable = false)
    private int maxStockThreshold;

    @Column(name = "OnReorder", nullable = false)
    private boolean onReorder;

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Decrements the quantity of this item in inventory.
     *
     * <p>If there is less available stock than {@code quantityDesired}, all
     * remaining stock is removed and the removed quantity is returned. The caller
     * is responsible for detecting the shortfall.
     *
     * @param quantityDesired number of units to remove; must be &gt; 0
     * @return the actual number of units removed from stock
     * @throws CatalogDomainException if stock is zero or {@code quantityDesired} is non-positive
     */
    public int removeStock(int quantityDesired) {
        if (availableStock == 0) {
            throw new CatalogDomainException(
                    "Empty stock, product item " + name + " is sold out");
        }
        if (quantityDesired <= 0) {
            throw new CatalogDomainException(
                    "Item units desired should be greater than zero");
        }

        int removed = Math.min(quantityDesired, availableStock);
        availableStock -= removed;
        return removed;
    }

    /**
     * Increments stock up to the configured {@code maxStockThreshold}.
     * Clears the {@code onReorder} flag on success.
     *
     * @param quantity number of units to add
     * @return the actual number of units added to stock
     */
    public int addStock(int quantity) {
        int original = availableStock;

        if ((availableStock + quantity) > maxStockThreshold) {
            // Only fill up to the maximum threshold
            availableStock += (maxStockThreshold - availableStock);
        } else {
            availableStock += quantity;
        }

        onReorder = false;
        return availableStock - original;
    }

    /**
     * Populates the transient {@code pictureUri} field.
     *
     * <p>When {@code azureStorageEnabled} is {@code true}, the URI is built by
     * appending {@code PictureFileName} to {@code baseUrl} directly.
     * When {@code false}, the placeholder {@code [0]} in {@code baseUrl} is
     * replaced with the item's {@code id} (matching the {@code PicController}
     * routing pattern {@code /api/v1/catalog/items/{id}/pic}).
     *
     * @param baseUrl              configured base URL, may contain {@code [0]} placeholder
     * @param azureStorageEnabled  {@code true} to use Azure Blob Storage URLs
     */
    public void fillProductUrl(String baseUrl, boolean azureStorageEnabled) {
        if (azureStorageEnabled) {
            pictureUri = baseUrl + pictureFileName;
        } else {
            pictureUri = baseUrl.replace("[0]", id == null ? "0" : id.toString());
        }
    }
}
