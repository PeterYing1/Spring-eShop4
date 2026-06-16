package com.eshop.ordering.domain.seedwork;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for DDD Value Objects.
 *
 * <p>Subclasses implement {@link #getEqualityComponents()} to define which
 * fields participate in equality comparison. Two value objects of the same
 * type are equal when all their equality components are equal.
 */
public abstract class ValueObject {

    /**
     * Returns the list of field values that define equality for this value object.
     *
     * @return ordered list of values; {@code null} elements are allowed and
     *         compared as {@code null}
     */
    protected abstract List<Object> getEqualityComponents();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        ValueObject other = (ValueObject) obj;
        return getEqualityComponents().equals(other.getEqualityComponents());
    }

    @Override
    public int hashCode() {
        return getEqualityComponents().stream()
                .map(obj -> obj != null ? obj.hashCode() : 0)
                .reduce(1, (a, b) -> 31 * a + b);
    }
}
