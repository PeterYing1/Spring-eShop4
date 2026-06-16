package com.eshop.ordering.domain.seedwork;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for DDD enumeration types — a richer alternative to
 * Java {@code enum} that supports static factory look-up and equality by id.
 *
 * <p>Concrete subclasses declare {@code public static final} fields of their
 * own type; {@link #getAll(Class)} reflects over those fields to return all
 * known instances.
 */
public abstract class Enumeration {

    private final int id;
    private final String name;

    protected Enumeration(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns all declared instances of the given {@link Enumeration} subclass
     * by reflecting on its public static fields.
     *
     * @param <T>   the enumeration type
     * @param clazz the class of the enumeration
     * @return list of all instances; never {@code null}
     */
    public static <T extends Enumeration> List<T> getAll(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && clazz.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    @SuppressWarnings("unchecked")
                    T instance = (T) field.get(null);
                    if (instance != null) {
                        result.add(instance);
                    }
                } catch (IllegalAccessException e) {
                    // should not happen after setAccessible(true)
                    throw new RuntimeException("Cannot access field " + field.getName(), e);
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Enumeration)) {
            return false;
        }
        Enumeration other = (Enumeration) obj;
        return getClass().equals(other.getClass()) && id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
