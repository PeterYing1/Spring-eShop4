package com.eshop.location.infrastructure;

import com.eshop.location.domain.GeoLocation;
import com.eshop.location.domain.Location;
import com.eshop.location.domain.UserLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB-backed repository for {@link Location} and {@link UserLocation} documents.
 *
 * <p>Uses {@link MongoTemplate} directly (rather than a Spring Data repository
 * interface) to mirror the .NET {@code LocationsRepository} which relied on the
 * raw MongoDB driver.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationsRepository {

    private final MongoTemplate mongoTemplate;

    // -------------------------------------------------------------------------
    // Location queries
    // -------------------------------------------------------------------------

    /**
     * Returns all locations stored in the {@code Locations} collection.
     */
    public List<Location> findAll() {
        return mongoTemplate.findAll(Location.class, "Locations");
    }

    /**
     * Finds a single location by its MongoDB {@code _id}.
     *
     * @param id the BSON ObjectId string
     * @return the location, or {@code null} if not found
     */
    public Location findById(String id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, Location.class, "Locations");
    }

    /**
     * Finds a location by its domain {@code locationId} field.
     *
     * @param locationId the application-level location identifier
     * @return the location, or {@code null} if not found
     */
    public Location findByLocationId(String locationId) {
        Query query = Query.query(Criteria.where("locationId").is(locationId));
        return mongoTemplate.findOne(query, Location.class, "Locations");
    }

    // -------------------------------------------------------------------------
    // UserLocation queries
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link UserLocation} record for the given user, or {@code null}
     * if no location has been recorded yet.
     *
     * @param userId the subject identifier of the user
     */
    public UserLocation findUserLocation(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, UserLocation.class, "UserLocation");
    }

    /**
     * Removes the {@link UserLocation} document for the given user.
     *
     * @param userId the subject identifier of the user
     * @return {@code true} (removal always considered successful)
     */
    public boolean deleteUserLocation(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        mongoTemplate.remove(query, UserLocation.class, "UserLocation");
        return true;
    }

    /**
     * Creates or updates the user's location based on the supplied geo-coordinates.
     *
     * <p>The nearest enclosing {@link Location} is determined by a simple
     * distance comparison across all known locations.  The first location
     * whose lat/lon is closest to the supplied coordinates is chosen.
     *
     * <p>If no existing {@link UserLocation} record is found for the user, a new
     * one is inserted; otherwise the existing record is updated in-place.
     *
     * @param userId      the subject identifier of the user
     * @param geoLocation the user's current geographic position
     * @return the upserted {@link UserLocation}
     */
    public UserLocation updateUserLocation(String userId, GeoLocation geoLocation) {
        // Find the nearest location by Euclidean distance on lat/lon
        List<Location> allLocations = findAll();
        Location nearest = findNearestLocation(allLocations, geoLocation);

        // Load or create the UserLocation document
        UserLocation userLocation = findUserLocation(userId);
        if (userLocation == null) {
            userLocation = new UserLocation();
            userLocation.setUserId(userId);
        }
        userLocation.setLocation(nearest);

        // Upsert: save() will insert if no _id, update if _id is present
        return mongoTemplate.save(userLocation, "UserLocation");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link Location} in {@code locations} that is geographically
     * closest to {@code geoLocation} using plain Euclidean distance on the
     * latitude/longitude values.
     *
     * <p>Returns {@code null} when the list is empty.
     */
    private Location findNearestLocation(List<Location> locations, GeoLocation geoLocation) {
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Location loc : locations) {
            double dLat = loc.getLatitude() - geoLocation.getLatitude();
            double dLon = loc.getLongitude() - geoLocation.getLongitude();
            double distance = Math.sqrt(dLat * dLat + dLon * dLon);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = loc;
            }
        }

        return nearest;
    }
}
