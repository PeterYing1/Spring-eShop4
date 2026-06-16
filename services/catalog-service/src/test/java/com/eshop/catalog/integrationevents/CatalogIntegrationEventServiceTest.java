package com.eshop.catalog.integrationevents;

import com.eshop.catalog.integrationevents.events.ProductPriceChangedIntegrationEvent;
import com.eshop.eventbus.IEventBus;
import com.eshop.eventlog.IIntegrationEventLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CatalogIntegrationEventService}.
 *
 * <p>No Spring context — Mockito is used to verify interactions between the service
 * and its collaborators ({@link IIntegrationEventLogService} and {@link IEventBus}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogIntegrationEventService — outbox / event-bus interactions")
class CatalogIntegrationEventServiceTest {

    @Mock
    private IIntegrationEventLogService eventLogService;

    @Mock
    private IEventBus eventBus;

    @InjectMocks
    private CatalogIntegrationEventService catalogIntegrationEventService;

    // -------------------------------------------------------------------------
    // saveEventAndCatalogContextChangesAsync
    // -------------------------------------------------------------------------

    /**
     * Calling {@link CatalogIntegrationEventService#saveEventAndCatalogContextChangesAsync}
     * should:
     * <ol>
     *   <li>Persist the event to the outbox via {@code IIntegrationEventLogService.saveEvent}.</li>
     *   <li>Mark it as in-progress before attempting publication.</li>
     *   <li>Publish it on the event bus.</li>
     *   <li>Mark it as published on success.</li>
     * </ol>
     */
    @Test
    @DisplayName("saveEventAndPublish_callsOutboxAndEventBus")
    void saveEventAndPublish_callsOutboxAndEventBus() {
        // Arrange
        ProductPriceChangedIntegrationEvent event =
                new ProductPriceChangedIntegrationEvent(42, new BigDecimal("19.99"), new BigDecimal("14.99"));

        // Act
        catalogIntegrationEventService.saveEventAndCatalogContextChangesAsync(event);

        // Assert — outbox: event was saved with its own id as the transaction key
        verify(eventLogService, times(1))
                .saveEvent(eq(event), eq(event.getId().toString()));

        // Assert — state transitions: in-progress → published
        verify(eventLogService, times(1)).markEventAsInProgress(event.getId());
        verify(eventLogService, times(1)).markEventAsPublished(event.getId());

        // Assert — event was handed to the broker
        verify(eventBus, times(1)).publish(event);

        // Assert — failure state should NOT be set on the happy path
        verify(eventLogService, never()).markEventAsFailed(any());
    }

    // -------------------------------------------------------------------------
    // publishThroughEventBusAsync — failure path
    // -------------------------------------------------------------------------

    /**
     * When {@link IEventBus#publish} throws an exception the service should
     * catch it and mark the event as failed rather than propagating the error.
     */
    @Test
    @DisplayName("publishThroughEventBusAsync_marksEventAsFailed_onPublishError")
    void publishThroughEventBusAsync_marksEventAsFailed_onPublishError() {
        // Arrange
        ProductPriceChangedIntegrationEvent event =
                new ProductPriceChangedIntegrationEvent(1, new BigDecimal("5.00"), new BigDecimal("3.00"));

        doThrow(new RuntimeException("Broker unavailable")).when(eventBus).publish(any());

        // Act — should not propagate the exception
        catalogIntegrationEventService.publishThroughEventBusAsync(event);

        // Assert — failed marker is set
        verify(eventLogService, times(1)).markEventAsFailed(event.getId());

        // Assert — published marker must NOT be set when publish failed
        verify(eventLogService, never()).markEventAsPublished(any());
    }
}
