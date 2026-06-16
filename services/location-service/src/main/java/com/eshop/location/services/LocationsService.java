package com.eshop.location.services;

import com.eshop.eventbus.IEventBus;
import com.eshop.location.domain.GeoLocation;
import com.eshop.location.domain.Location;
import com.eshop.location.domain.UserLocation;
import com.eshop.location.infrastructure.LocationsRepository;
import com.eshop.location.integrationevents.events.UserLocationUpdatedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for location operations.
 *
 * <p>Wraps {@link LocationsRepository} and adds logging.  The
 * {@link #updateUserLocation(String, GeoLocation)} method additionally
 * publishes a {@link UserLocationUpdatedIntegrationEvent} after persisting the
 * new user location so that other services (e.g. Marketing) can react.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationsService {

    private final LocationsRepository locationsRepository;
    private final IEventBus eventBus;

    // -------------------------------------------------------------------------
    // Location queries
    // -------------------------------------------------------------------------

    /**
     * Returns all known locations.
     */
    public List<Location> getAllLocations() {
        log.debug("Fetching all locations");
        return locationsRepository.findAll();
    }

    /**
     * Returns the location identified by the given MongoDB {@code _id}.
     *
     * @param id the BSON ObjectId string
     * @return the location, or {@code null} if not found
     */
    public Location getLocationById(String id) {
        log.debug("Fetching location by id: {}", id);
        return locationsRepository.findById(id);
    }

    /**
     * Returns the location identified by the application-level {@code locationId}.
     *
     * @param locationId the domain location identifier
     * @return the location, or {@code null} if not found
     */
    public Location getLocationByLocationId(String locationId) {
        log.debug("Fetching location by locationId: {}", locationId);
        return locationsRepository.findByLocationId(locationId);
    }

    // -------------------------------------------------------------------------
    // UserLocation queries
    // -------------------------------------------------------------------------

    /**
     * Returns the current recorded location for the given user.
     *
     * @param userId the subject identifier of the user
     * @return the user's location record, or {@code null} if none exists
     */
    public UserLocation getUserLocation(String userId) {
        log.debug("Fetching user location for userId: {}", userId);
        return locationsRepository.findUserLocation(userId);
    }

    /**
     * Updates the user's current location based on the supplied geo-coordinates
     * and publishes a {@link UserLocationUpdatedIntegrationEvent}.
     *
     * <p>The previous location id is captured before updating so it can be
     * included in the event.  On first update the previous location id is
     * {@code null}.
     *
     * @param userId      the subject identifier of the user
     * @param geoLocation the user's current geographic position
     * @return the upserted {@link UserLocation}
     */
    public UserLocation updateUserLocation(String userId, GeoLocation geoLocation) {
        log.info("Updating location for userId: {} — lat={}, lon={}",
                userId, geoLocation.getLatitude(), geoLocation.getLongitude());

        // Capture previous location id before overwriting
        UserLocation existing = locationsRepository.findUserLocation(userId);
        String previousLocationId = (existing != null && existing.getLocation() != null)
                ? existing.getLocation().getLocationId()
                : null;

        // Persist the new location
        UserLocation updated = locationsRepository.updateUserLocation(userId, geoLocation);

        // Derive current location id from the updated record
        String currentLocationId = (updated.getLocation() != null)
                ? updated.getLocation().getLocationId()
                : null;

        // Publish integration event
        UserLocationUpdatedIntegrationEvent event =
                new UserLocationUpdatedIntegrationEvent(userId, currentLocationId, previousLocationId);

        try {
            eventBus.publish(event);
            log.info("Published UserLocationUpdatedIntegrationEvent for userId: {}", userId);
        } catch (Exception ex) {
            log.error("ERROR publishing UserLocationUpdatedIntegrationEvent for userId: {} — {}",
                    userId, ex.getMessage(), ex);
            throw ex;
        }

        return updated;
    }
}
