package com.eshop.websupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Global REST exception handler that translates common exceptions into
 * structured JSON error responses matching the .NET source application's
 * {@code HttpGlobalExceptionFilter} behaviour.
 *
 * <p>Error response shape:
 * <pre>{@code
 * { "messages": ["Validation failed for field 'name': must not be blank"] }
 * }</pre>
 *
 * <p>Status code mapping:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} — {@code 400 Bad Request} with all field errors</li>
 *   <li>{@link RuntimeException} whose message contains "not found" (case-insensitive) — {@code 404 Not Found}</li>
 *   <li>Any other {@link RuntimeException} — {@code 500 Internal Server Error}</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Bean Validation failures ({@code @Valid} / {@code @Validated} on
     * controller method parameters).
     *
     * @param ex the validation exception
     * @return 400 with a list of human-readable field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> messages = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return "Validation failed for field '%s': %s"
                                .formatted(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                    return error.getDefaultMessage();
                })
                .toList();

        log.debug("Request validation failed: {}", messages);
        return ResponseEntity.badRequest().body(Map.of("messages", messages));
    }

    /**
     * Handles domain "not found" exceptions.  Any {@link RuntimeException}
     * whose message contains "not found" (case-insensitive) is mapped to a
     * 404 response.
     *
     * @param ex the exception
     * @return 404 with the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, List<String>>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();

        if (message != null && message.toLowerCase().contains("not found")) {
            log.debug("Resource not found: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("messages", List.of(message)));
        }

        log.error("Unhandled runtime exception: {}", message, ex);
        String safeMessage = message != null ? message : "An unexpected error occurred";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("messages", List.of(safeMessage)));
    }
}
