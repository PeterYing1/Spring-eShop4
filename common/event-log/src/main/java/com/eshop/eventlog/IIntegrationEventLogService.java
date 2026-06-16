package com.eshop.eventlog;

import com.eshop.eventbus.IntegrationEvent;

import java.util.List;
import java.util.UUID;

/**
 * Outbox service contract for integration events.
 *
 * <p>Services that require guaranteed at-least-once delivery (Catalog, Ordering)
 * use this service to:
 * <ol>
 *   <li>Save an event alongside domain changes in the same database transaction.</li>
 *   <li>After the transaction commits, mark the event as in-progress, publish it
 *       to RabbitMQ, and mark it as published.</li>
 *   <li>On failure, mark the event as failed for later retry.</li>
 * </ol>
 */
public interface IIntegrationEventLogService {

    /**
     * Returns all log entries that are pending publication for the given
     * transaction.  Only entries in {@link EventState#NOT_PUBLISHED} state
     * are returned.
     *
     * @param transactionId the transaction identifier associated with the entries
     * @return list of pending log entries, ordered by creation time ascending
     */
    List<IntegrationEventLogEntry> retrieveEventLogsPendingToPublish(UUID transactionId);

    /**
     * Persists a new log entry for {@code event} in {@link EventState#NOT_PUBLISHED}
     * state within the current database transaction.
     *
     * <p>The caller is responsible for ensuring this call participates in the
     * same transaction as the domain changes.  Use Spring's
     * {@code @Transactional(propagation = MANDATORY)} or pass the active
     * {@code EntityManager} to ensure transactional participation.
     *
     * @param event         the integration event to save
     * @param transactionId identifier of the current database transaction
     */
    void saveEvent(IntegrationEvent event, String transactionId);

    /**
     * Marks the log entry with the given {@code eventId} as
     * {@link EventState#IN_PROGRESS}.
     *
     * @param eventId UUID of the integration event
     */
    void markEventAsInProgress(UUID eventId);

    /**
     * Marks the log entry with the given {@code eventId} as
     * {@link EventState#PUBLISHED} and increments the sent counter.
     *
     * @param eventId UUID of the integration event
     */
    void markEventAsPublished(UUID eventId);

    /**
     * Marks the log entry with the given {@code eventId} as
     * {@link EventState#PUBLISHED_FAILED}.
     *
     * @param eventId UUID of the integration event
     */
    void markEventAsFailed(UUID eventId);
}
