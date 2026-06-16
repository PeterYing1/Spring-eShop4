package com.eshop.eventlog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code IntegrationEventLog} outbox table.
 *
 * <p>This table is created per service (Catalog, Ordering) that uses the
 * transactional outbox pattern to guarantee at-least-once delivery of
 * integration events to RabbitMQ.
 *
 * <p>SQL Server DDL (per service):
 * <pre>{@code
 * CREATE TABLE IntegrationEventLog (
 *     EventId       UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
 *     EventTypeName NVARCHAR(255) NOT NULL,
 *     State         INT NOT NULL,
 *     TimesSent     INT NOT NULL DEFAULT 0,
 *     CreationTime  DATETIME2 NOT NULL,
 *     Content       NVARCHAR(MAX) NOT NULL,
 *     TransactionId NVARCHAR(255) NULL
 * );
 * }</pre>
 */
@Entity
@Table(name = "IntegrationEventLog")
@Getter
@Setter
@NoArgsConstructor
public class IntegrationEventLogEntry {

    /** UUID primary key — matches the {@code Id} field of the integration event. */
    @Id
    @Column(name = "EventId", columnDefinition = "UNIQUEIDENTIFIER", updatable = false, nullable = false)
    private UUID eventId;

    /**
     * Fully-qualified class name of the event, e.g.
     * {@code com.eshop.catalog.integrationevents.events.ProductPriceChangedIntegrationEvent}.
     */
    @Column(name = "EventTypeName", nullable = false, length = 255)
    private String eventTypeName;

    /**
     * Current publication state.  Stored as an integer ordinal matching
     * {@link EventState#getValue()}.
     */
    @Column(name = "State", nullable = false)
    private int state;

    /** Number of times the event has been sent to the broker. */
    @Column(name = "TimesSent", nullable = false)
    private int timesSent = 0;

    /** Timestamp when the entry was first created. */
    @Column(name = "CreationTime", nullable = false, columnDefinition = "datetime2")
    private Instant creationTime;

    /**
     * Full JSON serialisation of the event payload using PascalCase property
     * names (Jackson {@code UPPER_CAMEL_CASE} naming strategy) to preserve
     * compatibility with the .NET source application.
     */
    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    /**
     * Optional identifier of the database transaction in which this event was
     * saved, used to group outbox events for atomic publishing.
     */
    @Column(name = "TransactionId", length = 255)
    private String transactionId;

    // -------------------------------------------------------------------------
    // Convenience factory
    // -------------------------------------------------------------------------

    /**
     * Creates a new log entry ready to be persisted.
     *
     * @param eventId       UUID of the integration event
     * @param eventTypeName fully-qualified class name of the event
     * @param content       JSON payload
     * @param transactionId optional transaction identifier
     * @return a new entry in {@link EventState#NOT_PUBLISHED} state
     */
    public static IntegrationEventLogEntry create(
            UUID eventId,
            String eventTypeName,
            String content,
            String transactionId) {

        IntegrationEventLogEntry entry = new IntegrationEventLogEntry();
        entry.eventId = eventId;
        entry.eventTypeName = eventTypeName;
        entry.state = EventState.NOT_PUBLISHED.getValue();
        entry.timesSent = 0;
        entry.creationTime = Instant.now();
        entry.content = content;
        entry.transactionId = transactionId;
        return entry;
    }

    // -------------------------------------------------------------------------
    // Domain transitions
    // -------------------------------------------------------------------------

    /** Transitions this entry to {@link EventState#IN_PROGRESS}. */
    public void markAsInProgress() {
        this.state = EventState.IN_PROGRESS.getValue();
    }

    /** Transitions this entry to {@link EventState#PUBLISHED} and increments the sent counter. */
    public void markAsPublished() {
        this.state = EventState.PUBLISHED.getValue();
        this.timesSent++;
    }

    /** Transitions this entry to {@link EventState#PUBLISHED_FAILED}. */
    public void markAsFailed() {
        this.state = EventState.PUBLISHED_FAILED.getValue();
    }

    /** Returns the current state as an {@link EventState} enum constant. */
    public EventState getEventState() {
        return EventState.fromValue(this.state);
    }
}
