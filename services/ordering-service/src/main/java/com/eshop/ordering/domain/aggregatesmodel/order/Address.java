package com.eshop.ordering.domain.aggregatesmodel.order;

import com.eshop.ordering.domain.seedwork.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Arrays;
import java.util.List;

/**
 * Address value object embedded inside {@link Order}.
 *
 * <p>Mapped as a JPA {@code @Embeddable} so all address columns are stored
 * directly in the {@code ordering.orders} table rather than a separate table.
 */
@Embeddable
public class Address extends ValueObject {

    @Column(name = "Address_Street")
    private String street;

    @Column(name = "Address_City")
    private String city;

    @Column(name = "Address_State")
    private String state;

    @Column(name = "Address_Country")
    private String country;

    @Column(name = "Address_ZipCode")
    private String zipCode;

    /** Required by JPA. */
    protected Address() {
    }

    public Address(String street, String city, String state, String country, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    protected List<Object> getEqualityComponents() {
        return Arrays.asList(street, city, state, country, zipCode);
    }
}
