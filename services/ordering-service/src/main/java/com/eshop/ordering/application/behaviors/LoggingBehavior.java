package com.eshop.ordering.application.behaviors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Cross-cutting behavior that logs the start, end, and duration of command
 * handler execution.
 *
 * <p>Wrap command handler calls with {@link #execute(String, Supplier)} to
 * automatically capture structured timing logs.
 */
@Component
public class LoggingBehavior {

    private static final Logger log = LoggerFactory.getLogger(LoggingBehavior.class);

    /**
     * Executes the given command supplier, logging start and finish with timing.
     *
     * @param <T>         the command result type
     * @param commandName short identifier of the command (e.g. "CancelOrderCommand")
     * @param action      the command handler invocation
     * @return the command result
     */
    public <T> T execute(String commandName, Supplier<T> action) {
        log.info("----- Handling command {} -----", commandName);
        long start = System.currentTimeMillis();
        try {
            T result = action.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("----- Command {} handled ({} ms) -----", commandName, elapsed);
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("----- Command {} FAILED after {} ms: {}", commandName, elapsed, ex.getMessage());
            throw ex;
        }
    }
}
