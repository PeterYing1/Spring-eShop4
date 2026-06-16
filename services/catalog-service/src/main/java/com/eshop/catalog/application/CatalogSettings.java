package com.eshop.catalog.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the Catalog service.
 *
 * <p>Bound from the {@code catalog} prefix in {@code application.yml}.
 * Example:
 * <pre>
 * catalog:
 *   pic-base-url: "http://localhost:5101/api/v1/catalog/items/[0]/pic/"
 *   azure-storage-enabled: false
 *   use-customization-data: false
 * </pre>
 */
@ConfigurationProperties(prefix = "catalog")
@Getter
@Setter
public class CatalogSettings {

    /**
     * Base URL template for product picture URIs.
     * When {@code azureStorageEnabled} is {@code false} the placeholder
     * {@code [0]} is replaced with the item id at read time.
     */
    private String picBaseUrl;

    /**
     * When {@code true}, picture URIs point directly to Azure Blob Storage
     * using {@code picBaseUrl + PictureFileName}.
     * When {@code false} (default), the {@code PicController} endpoint is used.
     */
    private boolean azureStorageEnabled = false;

    /**
     * Reserved for customization data scenarios (DevSpaces / demo).
     * Not used in production logic.
     */
    private boolean useCustomizationData = false;
}
