package com.eshop.ordering.domain.seedwork;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all domain entities.
 *
 * <p>Provides identity (auto-generated integer PK), domain event collection,
 * and value-based equality semantics keyed on the entity id.
 */
@MappedSuperclass
public abstract class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Transient
    private List<Object> domainEvents;

    public Integer getId() {
        return id;
    }

    protected void setId(Integer id) {
        this.id = id;
    }

    // -------------------------------------------------------------------------
    // Domain events
    // -------------------------------------------------------------------------

    public void addDomainEvent(Object event) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(event);
    }

    public void removeDomainEvent(Object event) {
        if (domainEvents != null) {
            domainEvents.remove(event);
        }
    }

    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }

    public List<Object> getDomainEvents() {
        if (domainEvents == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(domainEvents);
    }

    // -------------------------------------------------------------------------
    // Equality
    // -------------------------------------------------------------------------

    public boolean isTransient() {
        return this.id == null || this.id == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Entity)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        Entity other = (Entity) obj;
        if (isTransient() || other.isTransient()) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        if (!isTransient()) {
            return Objects.hash(id) ^ 31;
        }
        return super.hashCode();
    }
}
