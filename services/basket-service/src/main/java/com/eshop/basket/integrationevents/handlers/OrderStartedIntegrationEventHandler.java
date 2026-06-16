package com.eshop.basket.integrationevents.handlers;

import com.eshop.basket.domain.IBasketRepository;
import com.eshop.basket.integrationevents.events.OrderStartedIntegrationEvent;
import com.eshop.eventbus.IIntegrationEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles {@link OrderStartedIntegrationEvent} published by the Ordering service.
 *
 * <p>When an order has been accepted, the buyer's basket is no longer needed
 * and is deleted from Redis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStartedIntegrationEventHandler
        implements IIntegrationEventHandler<OrderStartedIntegrationEvent> {

    private final IBasketRepository basketRepository;

    @Override
    public void handle(OrderStartedIntegrationEvent event) {
        log.info("Handling integration event: {} - OrderStartedIntegrationEvent (userId={})",
                event.getId(), event.getUserId());

        basketRepository.deleteBasket(event.getUserId());

        log.info("Basket deleted for userId '{}'", event.getUserId());
    }
}
