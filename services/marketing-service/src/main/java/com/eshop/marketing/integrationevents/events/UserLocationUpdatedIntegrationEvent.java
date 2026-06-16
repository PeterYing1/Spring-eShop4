package com.eshop.marketing.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration event published by the Location service when a user's location
 * changes.  The Marketing service consumes this event to update its MongoDB
 * read model.
 *
 * <p>All field names use {@code @JsonProperty} with PascalCase names to match
 * the .NET Newtonsoft.Json serialisation convention used by the publisher.
 */
public class UserLocationUpdatedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("CurrentLocationId")
    private String currentLocationId;

    @JsonProperty("PreviousLocationId")
    private String previousLocationId;

    // Required for Jackson deserialisation
    public UserLocationUpdatedIntegrationEvent() {
        super();
    }

    public UserLocationUpdatedIntegrationEvent(
            String userId,
            String currentLocationId,
            String previousLocationId) {
        super();
        this.userId = userId;
        this.currentLocationId = currentLocationId;
        this.previousLocationId = previousLocationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrentLocationId() {
        return currentLocationId;
    }

    public String getPreviousLocationId() {
        return previousLocationId;
    }
}
