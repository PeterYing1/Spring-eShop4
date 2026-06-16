package com.eshop.marketing.infrastructure.mongo;

import com.eshop.marketing.domain.mongo.UserMarketingData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * MongoDB-backed repository for {@link UserMarketingData} documents.
 *
 * <p>Uses {@link MongoTemplate} directly rather than a Spring Data repository
 * interface so that atomic findAndModify / upsert operations can be issued.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class MongoMarketingRepository {

    private final MongoTemplate mongoTemplate;

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link UserMarketingData} document for the given user, or
     * {@code null} if no document exists yet.
     *
     * @param userId the JWT subject ({@code sub} claim) of the user
     * @return the document, or {@code null}
     */
    public UserMarketingData findByUserId(String userId) {
        Query query = Query.query(Criteria.where("_id").is(userId));
        return mongoTemplate.findOne(query, UserMarketingData.class);
    }

    // -------------------------------------------------------------------------
    // Write — location
    // -------------------------------------------------------------------------

    /**
     * Pushes a new {@link UserMarketingData.Location} entry into the user's
     * {@code locations} array, upserting the top-level document if it does not
     * exist yet.
     *
     * @param userId     the JWT subject of the user
     * @param locationId the identifier of the location visited
     * @param lat        latitude (pass {@code 0} when not available from the event)
     * @param lon        longitude (pass {@code 0} when not available from the event)
     */
    public void updateUserLocation(String userId, String locationId, double lat, double lon) {
        log.debug("Updating location for user={} locationId={}", userId, locationId);

        Query query = Query.query(Criteria.where("_id").is(userId));

        UserMarketingData.Location location = new UserMarketingData.Location();
        location.setId(locationId);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setUpdatedDate(Instant.now());

        Update update = new Update()
                .push("locations", location)
                .setOnInsert("_id", userId);

        mongoTemplate.upsert(query, update, UserMarketingData.class);
    }

    // -------------------------------------------------------------------------
    // Write — activity
    // -------------------------------------------------------------------------

    /**
     * Pushes a new {@link UserMarketingData.UserActivityLog} entry into the
     * user's {@code activityLog} array, upserting the top-level document if it
     * does not exist yet.
     *
     * @param userId       the JWT subject of the user
     * @param activityType a string describing the activity type (e.g. "ViewProduct")
     * @param activityId   the identifier of the activity target
     */
    public void addUserActivity(String userId, String activityType, String activityId) {
        log.debug("Adding activity for user={} type={} id={}", userId, activityType, activityId);

        Query query = Query.query(Criteria.where("_id").is(userId));

        UserMarketingData.UserActivityLog entry = new UserMarketingData.UserActivityLog();
        entry.setActivityType(activityType);
        entry.setActivityId(activityId);
        entry.setUpdatedDate(Instant.now());

        Update update = new Update()
                .push("activityLog", entry)
                .setOnInsert("_id", userId);

        mongoTemplate.upsert(query, update, UserMarketingData.class);
    }
}
