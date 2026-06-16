package com.eshop.marketing.domain.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document storing the marketing read model for a single user.
 *
 * <p>Stored in the {@code UserMarketingData} collection in {@code MarketingDb}.
 * The document is keyed by {@code userId} (the JWT subject / sub claim).
 */
@Document(collection = "UserMarketingData")
@Data
@NoArgsConstructor
public class UserMarketingData {

    @Id
    private String userId;

    private List<Location> locations = new ArrayList<>();

    private List<UserActivityLog> activityLog = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Embedded value types
    // -------------------------------------------------------------------------

    /**
     * A single location visit recorded for the user.
     */
    @Data
    @NoArgsConstructor
    public static class Location {
        private String id;
        private double latitude;
        private double longitude;
        private Instant updatedDate;
    }

    /**
     * A single user-activity entry (e.g. viewed a product, placed an order).
     */
    @Data
    @NoArgsConstructor
    public static class UserActivityLog {
        private String activityType;
        private String activityId;
        private Instant updatedDate;
    }
}
