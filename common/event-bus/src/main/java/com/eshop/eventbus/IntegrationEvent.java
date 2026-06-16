package com.eshop.eventbus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all integration events published over the event bus.
 * Properties use PascalCase JSON names to match the .NET source application's
 * Newtonsoft.Json serialisation convention.
 */
public abstract class IntegrationEvent {

    @JsonProperty("Id")
    private final UUID id;

    @JsonProperty("CreationDate")
    private final Instant creationDate;

    protected IntegrationEvent() {
        this.id = UUID.randomUUID();
        this.creationDate = Instant.now();
    }

    protected IntegrationEvent(UUID id, Instant creationDate) {
        this.id = id;
        this.creationDate = creationDate;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
}
