package com.eshop.eventlog;

/**
 * State machine for integration event log entries (outbox pattern).
 *
 * <p>Persisted as its ordinal integer to match the SQL Server schema:
 * <pre>
 *   0 = NOT_PUBLISHED
 *   1 = IN_PROGRESS
 *   2 = PUBLISHED
 *   3 = PUBLISHED_FAILED
 * </pre>
 */
public enum EventState {

    /** Event has been saved to the outbox but not yet dispatched to RabbitMQ. */
    NOT_PUBLISHED(0),

    /** Publishing is in progress; prevents duplicate delivery on concurrent workers. */
    IN_PROGRESS(1),

    /** Event was successfully published to RabbitMQ. */
    PUBLISHED(2),

    /** All publish attempts have failed; manual intervention may be required. */
    PUBLISHED_FAILED(3);

    private final int value;

    EventState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Resolves the enum constant from its integer value.
     *
     * @param value the numeric state stored in the database
     * @return the matching {@link EventState}
     * @throws IllegalArgumentException if no match is found
     */
    public static EventState fromValue(int value) {
        for (EventState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown EventState value: " + value);
    }
}
