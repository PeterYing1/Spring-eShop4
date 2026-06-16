package com.eshop.marketing.integrationevents.handlers;

import com.eshop.eventbus.IIntegrationEventHandler;
import com.eshop.marketing.infrastructure.mongo.MongoMarketingRepository;
import com.eshop.marketing.integrationevents.events.UserLocationUpdatedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Handles {@link UserLocationUpdatedIntegrationEvent} messages delivered to the
 * {@code Marketing} RabbitMQ queue.
 *
 * <p>When a user's location changes (published by the Location service), this
 * handler updates the Marketing MongoDB read model so that campaign matching
 * can use the user's current location.
 *
 * <p>Latitude and longitude are not carried in the event payload; they default
 * to {@code 0} until a richer event contract is available.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLocationUpdatedIntegrationEventHandler
        implements IIntegrationEventHandler<UserLocationUpdatedIntegrationEvent> {

    private final MongoMarketingRepository mongoMarketingRepository;

    @RabbitListener(queues = "Marketing",
            id = "marketingLocationListener",
            containerFactory = "rabbitListenerContainerFactory")
    @Override
    public void handle(UserLocationUpdatedIntegrationEvent event) throws Exception {
        log.info("Handling integration event UserLocationUpdated (id={}, userId={}, locationId={})",
                event.getId(), event.getUserId(), event.getCurrentLocationId());

        mongoMarketingRepository.updateUserLocation(
                event.getUserId(),
                event.getCurrentLocationId(),
                0,   // latitude not in event
                0    // longitude not in event
        );

        log.info("Location updated in MongoDB for user={} locationId={}",
                event.getUserId(), event.getCurrentLocationId());
    }
}
