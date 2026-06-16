package com.eshop.catalog.integrationevents;

import com.eshop.eventbus.IEventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.eshop.eventlog.IIntegrationEventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that implements the transactional outbox pattern for Catalog.
 *
 * <p>Each call to {@link #saveEventAndCatalogContextChangesAsync(IntegrationEvent)}
 * persists the event to the {@code IntegrationEventLog} table in the same transaction
 * as the current domain changes, then publishes it to RabbitMQ and marks it published.
 *
 * <p>This guarantees that event publication and database mutation are atomic —
 * the event is never lost even if the broker is temporarily unavailable (it can
 * be replayed from the outbox table).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogIntegrationEventService {

    private final IIntegrationEventLogService eventLogService;
    private final IEventBus eventBus;

    /**
     * Saves the integration event to the outbox table within the current
     * transaction, then publishes it to RabbitMQ and marks it as published.
     *
     * <p>If publication fails the event is marked as {@code PUBLISHED_FAILED}
     * so it can be retried later.
     *
     * @param event the integration event to save and publish
     */
    @Transactional
    public void saveEventAndCatalogContextChangesAsync(IntegrationEvent event) {
        log.info("Saving integration event {} (id={}) to outbox",
                event.getClass().getSimpleName(), event.getId());

        // Save to outbox — must participate in the caller's transaction
        eventLogService.saveEvent(event, event.getId().toString());

        publishThroughEventBusAsync(event);
    }

    /**
     * Publishes an event that has already been saved to the outbox.
     * Marks the event as {@code IN_PROGRESS} before sending and either
     * {@code PUBLISHED} on success or {@code PUBLISHED_FAILED} on error.
     *
     * @param event the integration event to publish
     */
    public void publishThroughEventBusAsync(IntegrationEvent event) {
        try {
            log.info("Publishing integration event {} (id={})",
                    event.getClass().getSimpleName(), event.getId());

            eventLogService.markEventAsInProgress(event.getId());
            eventBus.publish(event);
            eventLogService.markEventAsPublished(event.getId());

            log.info("Integration event {} published successfully", event.getId());
        } catch (Exception ex) {
            log.error("Failed to publish integration event {} (id={}): {}",
                    event.getClass().getSimpleName(), event.getId(), ex.getMessage(), ex);
            eventLogService.markEventAsFailed(event.getId());
        }
    }
}
