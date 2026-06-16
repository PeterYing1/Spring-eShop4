package com.eshop.eventlog;

import com.eshop.eventbus.IntegrationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * JPA-backed implementation of {@link IIntegrationEventLogService}.
 *
 * <p>Serialises events to JSON using PascalCase property names (matching the
 * .NET Newtonsoft.Json convention) before storing them in the
 * {@code IntegrationEventLog} table.
 *
 * <p>The {@link #saveEvent(IntegrationEvent, String)} method uses
 * {@link Propagation#MANDATORY} to ensure it always participates in an existing
 * transaction started by the domain service — this is the core invariant of the
 * outbox pattern.
 */
@Service
@Transactional
public class IntegrationEventLogService implements IIntegrationEventLogService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationEventLogService.class);

    private final IntegrationEventLogRepository repository;

    /**
     * PascalCase ObjectMapper used exclusively for serialising event payloads
     * into the {@code Content} column of the outbox table.
     */
    private final ObjectMapper eventObjectMapper;

    public IntegrationEventLogService(IntegrationEventLogRepository repository) {
        this.repository = repository;
        this.eventObjectMapper = buildEventObjectMapper();
    }

    private static ObjectMapper buildEventObjectMapper() {
        return new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // -------------------------------------------------------------------------
    // IIntegrationEventLogService
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationEventLogEntry> retrieveEventLogsPendingToPublish(UUID transactionId) {
        return repository.findByTransactionIdAndStateOrderByCreationTimeAsc(
                transactionId.toString(),
                EventState.NOT_PUBLISHED.getValue());
    }

    /**
     * Saves the event as a new outbox entry in {@link EventState#NOT_PUBLISHED}
     * state.  This method MUST be called within an active transaction so that
     * the outbox write and the domain write are atomic.
     *
     * @param event         the integration event
     * @param transactionId the transaction id (string representation of the
     *                      JPA transaction's underlying JDBC connection id, or
     *                      any correlation id meaningful to the caller)
     * @throws RuntimeException if JSON serialisation fails
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(IntegrationEvent event, String transactionId) {
        String content;
        try {
            content = eventObjectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise integration event {}: {}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialise integration event", e);
        }

        IntegrationEventLogEntry entry = IntegrationEventLogEntry.create(
                event.getId(),
                event.getClass().getName(),
                content,
                transactionId);

        repository.save(entry);
        log.debug("Saved integration event {} (id={}) to outbox for tx={}",
                event.getClass().getSimpleName(), event.getId(), transactionId);
    }

    @Override
    public void markEventAsInProgress(UUID eventId) {
        updateState(eventId, entry -> entry.markAsInProgress());
    }

    @Override
    public void markEventAsPublished(UUID eventId) {
        updateState(eventId, entry -> entry.markAsPublished());
    }

    @Override
    public void markEventAsFailed(UUID eventId) {
        updateState(eventId, entry -> entry.markAsFailed());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void updateState(UUID eventId, java.util.function.Consumer<IntegrationEventLogEntry> updater) {
        repository.findById(eventId).ifPresentOrElse(
                entry -> {
                    updater.accept(entry);
                    repository.save(entry);
                },
                () -> log.warn("IntegrationEventLogEntry not found for eventId={}", eventId)
        );
    }
}
