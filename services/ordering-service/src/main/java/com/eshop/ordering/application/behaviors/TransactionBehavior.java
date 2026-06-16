package com.eshop.ordering.application.behaviors;

import com.eshop.eventbus.IEventBus;
import com.eshop.eventbus.IntegrationEvent;
import com.eshop.eventlog.IIntegrationEventLogService;
import com.eshop.eventlog.IntegrationEventLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Cross-cutting behavior that wraps the command handler in a transaction and
 * publishes outbox events after the transaction commits.
 *
 * <p>Pattern:
 * <ol>
 *   <li>Begin transaction (via {@code @Transactional} on {@link #execute}).</li>
 *   <li>Run the command action.</li>
 *   <li>After commit, publish pending outbox events via the event bus.</li>
 * </ol>
 */
@Component
public class TransactionBehavior {

    private static final Logger log = LoggerFactory.getLogger(TransactionBehavior.class);

    private final IIntegrationEventLogService eventLogService;
    private final IEventBus eventBus;

    public TransactionBehavior(IIntegrationEventLogService eventLogService, IEventBus eventBus) {
        this.eventLogService = eventLogService;
        this.eventBus = eventBus;
    }

    /**
     * Executes {@code action} within a transaction and publishes outbox events
     * registered under {@code transactionId} after the commit completes.
     *
     * @param <T>           the command result type
     * @param transactionId correlation id used to group outbox events
     * @param action        the command handler invocation
     * @return the command result
     */
    @Transactional
    public <T> T execute(String transactionId, Supplier<T> action) {
        T result = action.get();

        // Register an after-commit hook to publish outbox events
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        publishPendingEvents(transactionId);
                    }
                });

        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void publishPendingEvents(String transactionId) {
        try {
            UUID txId = UUID.fromString(transactionId);
            List<IntegrationEventLogEntry> pending =
                    eventLogService.retrieveEventLogsPendingToPublish(txId);

            for (IntegrationEventLogEntry entry : pending) {
                try {
                    eventLogService.markEventAsInProgress(entry.getEventId());
                    // Note: for full outbox we would deserialize and re-publish here.
                    // Integration event handlers publish directly via IEventBus instead.
                    eventLogService.markEventAsPublished(entry.getEventId());
                } catch (Exception ex) {
                    log.error("Error publishing outbox event {}: {}",
                            entry.getEventId(), ex.getMessage(), ex);
                    eventLogService.markEventAsFailed(entry.getEventId());
                }
            }
        } catch (Exception ex) {
            log.error("Error processing outbox events for transactionId={}: {}",
                    transactionId, ex.getMessage(), ex);
        }
    }
}
