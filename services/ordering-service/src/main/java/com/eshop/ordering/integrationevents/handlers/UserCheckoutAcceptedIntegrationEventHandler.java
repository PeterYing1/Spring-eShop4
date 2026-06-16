package com.eshop.ordering.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.ordering.application.commands.CreateOrderCommand;
import com.eshop.ordering.application.commands.handlers.CreateOrderCommandHandler;
import com.eshop.ordering.integrationevents.events.OrderStartedIntegrationEvent;
import com.eshop.ordering.integrationevents.events.UserCheckoutAcceptedIntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles {@link UserCheckoutAcceptedIntegrationEvent} from the Basket service.
 *
 * <p>Translates the checkout event into a {@link CreateOrderCommand} and
 * executes the command handler to create a new order.
 */
@Component
public class UserCheckoutAcceptedIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(
            UserCheckoutAcceptedIntegrationEventHandler.class);

    private final CreateOrderCommandHandler createOrderCommandHandler;
    private final IEventBus eventBus;

    public UserCheckoutAcceptedIntegrationEventHandler(
            CreateOrderCommandHandler createOrderCommandHandler,
            IEventBus eventBus) {
        this.createOrderCommandHandler = createOrderCommandHandler;
        this.eventBus = eventBus;
    }

    public void handle(UserCheckoutAcceptedIntegrationEvent event) throws Exception {
        log.info("Handling UserCheckoutAcceptedIntegrationEvent for user '{}' requestId={}",
                event.getUserId(), event.getRequestId());

        List<CreateOrderCommand.OrderItemDTO> items = new ArrayList<>();
        if (event.getBasket() != null && event.getBasket().getItems() != null) {
            for (UserCheckoutAcceptedIntegrationEvent.BasketItemDTO basketItem
                    : event.getBasket().getItems()) {
                items.add(new CreateOrderCommand.OrderItemDTO(
                        basketItem.getProductId(),
                        basketItem.getProductName(),
                        basketItem.getUnitPrice(),
                        BigDecimal.ZERO,
                        basketItem.getQuantity(),
                        basketItem.getPictureUrl()));
            }
        }

        CreateOrderCommand command = new CreateOrderCommand(
                event.getUserId(),
                event.getUserName(),
                event.getCity(),
                event.getStreet(),
                event.getState(),
                event.getCountry(),
                event.getZipCode(),
                event.getCardNumber(),
                event.getCardHolderName(),
                event.getCardExpiration(),
                event.getCardSecurityNumber(),
                event.getCardTypeId(),
                items);

        int orderId = createOrderCommandHandler.handle(command);

        // Publish OrderStarted so the Basket service clears the cart
        eventBus.publish(new OrderStartedIntegrationEvent(event.getUserId()));
        log.info("Order #{} created from checkout event for user '{}'", orderId, event.getUserId());
    }
}
