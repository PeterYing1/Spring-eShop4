package com.eshop.marketing.domain.sql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Campaign} entities.
 *
 * <p>Provides a derived query method that returns all campaigns currently
 * active at the given instant — i.e. campaigns where {@code from < now} and
 * {@code to > now}.
 */
public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

    /**
     * Returns campaigns whose active window contains the given instants.
     *
     * <p>Usage: {@code findByFromBeforeAndToAfter(now, now)} returns all
     * campaigns where {@code from < now} and {@code to > now}.
     *
     * @param from reference instant for the {@code from} boundary check
     * @param to   reference instant for the {@code to} boundary check
     * @return list of active campaigns; empty if none match
     */
    List<Campaign> findByFromBeforeAndToAfter(Instant from, Instant to);
}
