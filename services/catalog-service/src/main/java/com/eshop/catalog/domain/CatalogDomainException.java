package com.eshop.catalog.domain;

/**
 * Domain exception thrown when a business rule in the Catalog domain is violated,
 * e.g. attempting to remove stock from an empty item or passing an invalid quantity.
 */
public class CatalogDomainException extends RuntimeException {

    public CatalogDomainException(String message) {
        super(message);
    }

    public CatalogDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
