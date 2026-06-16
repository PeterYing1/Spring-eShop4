package com.eshop.location.api;

import com.eshop.location.domain.GeoLocation;
import com.eshop.location.domain.Location;
import com.eshop.location.domain.UserLocation;
import com.eshop.location.services.LocationsService;
import com.eshop.security.IIdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for location operations.
 *
 * <p>Mirrors the .NET {@code LocationsController} behaviour:
 * <ul>
 *   <li>GET  {@code /}          — return all locations (no auth required per design spec)</li>
 *   <li>GET  {@code /{id}}      — return a single location by id; 404 if not found</li>
 *   <li>GET  {@code /user}      — return the current user's location; 404 if none</li>
 *   <li>PUT  {@code /user}      — update the current user's location; returns 201 Created</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/locations")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class LocationsController {

    private final LocationsService locationsService;
    private final IIdentityService identityService;

    // -------------------------------------------------------------------------
    // GET /api/v1/locations/
    // -------------------------------------------------------------------------

    /**
     * Returns all known locations.
     *
     * <p>Authentication is not required for this endpoint — it is publicly
     * accessible to allow the WebMVC app to render location lists without
     * an active session.
     *
     * @return HTTP 200 with the full list of locations
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Location>> getAllLocations() {
        log.debug("GET /api/v1/locations");
        List<Location> locations = locationsService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/locations/{id}
    // -------------------------------------------------------------------------

    /**
     * Returns a single location by its {@code id}.
     *
     * @param id the location id (MongoDB ObjectId or domain locationId)
     * @return HTTP 200 with the location, or HTTP 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocation(@PathVariable String id) {
        log.debug("GET /api/v1/locations/{}", id);
        Location location = locationsService.getLocationById(id);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/locations/user
    // -------------------------------------------------------------------------

    /**
     * Returns the current user's recorded location.
     *
     * @return HTTP 200 with the user location, or HTTP 404 if none has been set
     */
    @GetMapping("/user")
    public ResponseEntity<UserLocation> getUserLocation() {
        String userId = identityService.getUserIdentity();
        log.debug("GET /api/v1/locations/user — userId={}", userId);
        UserLocation userLocation = locationsService.getUserLocation(userId);
        if (userLocation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userLocation);
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/locations/user
    // -------------------------------------------------------------------------

    /**
     * Updates the current user's location based on the supplied geo-coordinates.
     *
     * <p>After persisting the update a {@code UserLocationUpdatedIntegrationEvent}
     * is published by the service layer.
     *
     * @param geoLocation the user's current geographic position
     * @return HTTP 201 Created
     */
    @PutMapping("/user")
    public ResponseEntity<Void> updateUserLocation(@RequestBody GeoLocation geoLocation) {
        String userId = identityService.getUserIdentity();
        log.info("PUT /api/v1/locations/user — userId={}, lat={}, lon={}",
                userId, geoLocation.getLatitude(), geoLocation.getLongitude());
        locationsService.updateUserLocation(userId, geoLocation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
