package com.eshop.marketing.api;

import com.eshop.marketing.domain.mongo.UserMarketingData;
import com.eshop.marketing.domain.sql.Campaign;
import com.eshop.marketing.domain.sql.CampaignRepository;
import com.eshop.marketing.infrastructure.mongo.MongoMarketingRepository;
import com.eshop.security.IIdentityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for the Marketing service.
 *
 * <p>Routes:
 * <ul>
 *   <li>{@code GET  /api/v1/campaigns}               — list all currently active campaigns</li>
 *   <li>{@code GET  /api/v1/campaigns/{id}}           — get a single campaign by id</li>
 *   <li>{@code GET  /api/v1/campaigns/user}           — get the requesting user's marketing data</li>
 *   <li>{@code PUT  /api/v1/campaigns/user/location}  — update the requesting user's location</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/campaigns")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class MarketingDataController {

    private final CampaignRepository campaignRepository;
    private final MongoMarketingRepository mongoMarketingRepository;
    private final IIdentityService identityService;

    // -------------------------------------------------------------------------
    // GET /api/v1/campaigns
    // -------------------------------------------------------------------------

    /**
     * Returns all campaigns that are currently active (from &lt;= now &lt;= to).
     *
     * @return 200 OK with list of active {@link Campaign} entities
     */
    @GetMapping
    public ResponseEntity<List<Campaign>> getAllActiveCampaigns() {
        Instant now = Instant.now();
        List<Campaign> campaigns = campaignRepository.findByFromBeforeAndToAfter(now, now);
        log.debug("Returning {} active campaign(s)", campaigns.size());
        return ResponseEntity.ok(campaigns);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/campaigns/{id}
    // -------------------------------------------------------------------------

    /**
     * Returns the campaign with the given id.
     *
     * @param id the campaign primary key
     * @return 200 OK with the campaign, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Campaign> getCampaignById(@PathVariable int id) {
        return campaignRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/campaigns/user
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link UserMarketingData} document for the currently
     * authenticated user.
     *
     * @return 200 OK with the user's marketing data, or 404 if not found
     */
    @GetMapping("/user")
    public ResponseEntity<UserMarketingData> getUserMarketingData() {
        String userId = identityService.getUserIdentity();
        log.debug("Fetching marketing data for user={}", userId);

        UserMarketingData data = mongoMarketingRepository.findByUserId(userId);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/campaigns/user/location
    // -------------------------------------------------------------------------

    /**
     * Updates the requesting user's last known location in the MongoDB read model.
     *
     * @param locationUpdate request body containing the {@code locationId}
     * @return 200 OK
     */
    @PutMapping("/user/location")
    public ResponseEntity<Void> updateUserLocation(@RequestBody LocationUpdate locationUpdate) {
        String userId = identityService.getUserIdentity();
        log.info("Updating location for user={} to locationId={}",
                userId, locationUpdate.getLocationId());

        mongoMarketingRepository.updateUserLocation(
                userId,
                locationUpdate.getLocationId(),
                0,
                0
        );

        return ResponseEntity.ok().build();
    }
}
