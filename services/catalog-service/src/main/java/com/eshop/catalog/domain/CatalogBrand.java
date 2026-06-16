package com.eshop.catalog.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lookup entity representing a product brand in the catalog.
 * Maps to the {@code CatalogBrand} table.
 */
@Entity
@Table(name = "CatalogBrand")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CatalogBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Brand", nullable = false, length = 100)
    private String brand;
}
