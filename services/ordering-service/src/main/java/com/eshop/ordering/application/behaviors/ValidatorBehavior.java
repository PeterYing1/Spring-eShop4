package com.eshop.ordering.application.behaviors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Cross-cutting behavior that validates a command using Jakarta Bean Validation
 * before forwarding to the handler.
 *
 * <p>Throws {@link ConstraintViolationException} if any constraint is violated.
 */
@Component
public class ValidatorBehavior {

    private static final Logger log = LoggerFactory.getLogger(ValidatorBehavior.class);

    private final Validator validator;

    public ValidatorBehavior(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validates {@code command} and, if valid, invokes {@code action}.
     *
     * @param <T>     the command result type
     * @param command the command to validate
     * @param action  the command handler invocation
     * @return the command result
     * @throws ConstraintViolationException if the command fails validation
     */
    public <T, C> T execute(C command, Supplier<T> action) {
        Set<ConstraintViolation<C>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            log.error("Command {} validation failed: {}", command.getClass().getSimpleName(), violations);
            throw new ConstraintViolationException(violations);
        }
        return action.get();
    }
}
