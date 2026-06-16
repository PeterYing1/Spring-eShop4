package com.eshop.ordering.application;

import com.eshop.ordering.application.commands.handlers.IdentifiedCommandHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdentifiedCommandHandler Tests")
class IdentifiedCommandHandlerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private IdentifiedCommandHandler handler;

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handle_newRequestId_executesInnerHandler: when no row exists for requestId, inner action is invoked")
    void handle_newRequestId_executesInnerHandler() {
        UUID requestId = UUID.randomUUID();

        // Simulate: no existing row → COUNT returns 0
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(0);
        // Simulate: INSERT succeeds
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), any()))
                .thenReturn(1);

        AtomicInteger callCount = new AtomicInteger(0);
        Supplier<Boolean> action = () -> {
            callCount.incrementAndGet();
            return true;
        };

        Boolean result = handler.execute(requestId, "TestCommand", action, false);

        assertTrue(result, "Inner action result should be returned");
        assertEquals(1, callCount.get(), "Inner action should be called exactly once");
    }

    @Test
    @DisplayName("handle_duplicateRequestId_returnsCachedResult: when row exists, inner handler is skipped")
    void handle_duplicateRequestId_returnsCachedResult() {
        UUID requestId = UUID.randomUUID();

        // Simulate: existing row → COUNT returns 1
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(1);

        AtomicInteger callCount = new AtomicInteger(0);
        Supplier<Boolean> action = () -> {
            callCount.incrementAndGet();
            return true;
        };

        Boolean result = handler.execute(requestId, "TestCommand", action, false);

        assertFalse(result, "Default result should be returned for duplicate request");
        assertEquals(0, callCount.get(), "Inner action should NOT be called for a duplicate");
        // No INSERT should happen for a duplicate
        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("handle_newRequestId_insertsRecord: on first execution a record is inserted into ordering.requests")
    void handle_newRequestId_insertsRecord() {
        UUID requestId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(0);
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), any()))
                .thenReturn(1);

        handler.execute(requestId, "CreateOrder", () -> 42, -1);

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO ordering.requests"),
                anyString(), anyString(), any());
    }

    @Test
    @DisplayName("handle_newRequestId_withIntegerResult_returnsActionResult")
    void handle_newRequestId_withIntegerResult_returnsActionResult() {
        UUID requestId = UUID.randomUUID();

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenReturn(0);
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), any()))
                .thenReturn(1);

        Integer result = handler.execute(requestId, "CreateOrder", () -> 99, -1);

        assertEquals(99, result);
    }
}
