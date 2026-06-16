package com.eshop.catalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lookup entity representing a product type in the catalog.
 * Maps to the {@code CatalogType} table.
 */
@Entity
@Table(name = "CatalogType")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Type", nullable = false, length = 100)
    private String type;
}
