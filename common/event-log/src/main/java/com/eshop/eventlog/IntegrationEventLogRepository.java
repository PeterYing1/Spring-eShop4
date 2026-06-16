package com.eshop.eventlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link IntegrationEventLogEntry}.
 *
 * <p>Each service that uses the outbox pattern will have its own instance of
 * this repository bound to its own data source and {@code IntegrationEventLog}
 * table.
 */
@Repository
public interface IntegrationEventLogRepository
        extends JpaRepository<IntegrationEventLogEntry, UUID> {

    /**
     * Returns all entries for the given transaction that are still pending
     * publication ({@link EventState#NOT_PUBLISHED}), ordered by creation time.
     *
     * @param transactionId the transaction identifier
     * @param state         the integer value of {@link EventState#NOT_PUBLISHED} (0)
     * @return ordered list of pending entries
     */
    List<IntegrationEventLogEntry> findByTransactionIdAndStateOrderByCreationTimeAsc(
            String transactionId, int state);
}
