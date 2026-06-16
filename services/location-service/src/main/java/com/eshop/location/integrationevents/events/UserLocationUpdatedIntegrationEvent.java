package com.eshop.location.integrationevents.events;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Published when a user's current location changes.
 *
 * <p>Consumed by the Marketing service to update the
 * {@code MarketingReadDataModel} MongoDB projection for the user.
 *
 * <p>All field names use {@code @JsonProperty} with PascalCase names to match
 * the .NET Newtonsoft.Json serialisation convention expected by consumers.
 */
public class UserLocationUpdatedIntegrationEvent extends IntegrationEvent {

    @JsonProperty("UserId")
    private final String userId;

    @JsonProperty("CurrentLocationId")
    private final String currentLocationId;

    @JsonProperty("PreviousLocationId")
    private final String previousLocationId;

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
