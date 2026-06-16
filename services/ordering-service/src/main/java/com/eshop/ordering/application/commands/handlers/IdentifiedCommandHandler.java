package com.eshop.ordering.application.commands.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Idempotency wrapper for command handlers.
 *
 * <p>Records each request id in the {@code ordering.requests} table on first
 * execution and returns a default result on duplicate requests.
 *
 * <p>Usage:
 * <pre>
 *   boolean result = identifiedCommandHandler.execute(requestId, "CancelOrder",
 *       () -> cancelOrderCommandHandler.handle(command));
 * </pre>
 */
@Service
public class IdentifiedCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(IdentifiedCommandHandler.class);

    private final JdbcTemplate jdbc;

    public IdentifiedCommandHandler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Executes the given command supplier only once per {@code requestId}.
     *
     * <p>If the request id has already been seen, the duplicate is silently
     * ignored and the provided {@code defaultResult} is returned.
     *
     * @param <T>           the command result type
     * @param requestId     the idempotency key from {@code x-requestid} header
     * @param commandName   human-readable command name for logging
     * @param commandAction the action to execute on first call
     * @param defaultResult result to return on duplicate
     * @return the command result or {@code defaultResult} on duplicate
     */
    @Transactional
    public <T> T execute(UUID requestId, String commandName,
                         Supplier<T> commandAction, T defaultResult) {
        if (existsRequest(requestId)) {
            log.warn("Duplicate request detected: requestId={} commandName={}. Returning default.",
                    requestId, commandName);
            return defaultResult;
        }

        createRequestRecord(requestId, commandName);

        log.info("Executing command {} for requestId={}", commandName, requestId);
        return commandAction.get();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean existsRequest(UUID requestId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(1) FROM ordering.requests WHERE Id = ?",
                Integer.class,
                requestId.toString());
        return count != null && count > 0;
    }

    private void createRequestRecord(UUID requestId, String commandName) {
        jdbc.update(
                "INSERT INTO ordering.requests (Id, Name, Time) VALUES (?, ?, ?)",
                requestId.toString(),
                commandName,
                Timestamp.from(Instant.now()));
    }
}
