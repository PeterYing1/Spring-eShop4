package com.eshop.payment.integrationevents.handlers;

import com.eshop.eventbus.IEventBus;
import com.eshop.eventbus.IIntegrationEventHandler;
import com.eshop.payment.integrationevents.events.OrderPaymentFailedIntegrationEvent;
import com.eshop.payment.integrationevents.events.OrderPaymentSucceededIntegrationEvent;
import com.eshop.payment.integrationevents.events.OrderStatusChangedToStockConfirmedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles {@link OrderStatusChangedToStockConfirmedIntegrationEvent} published by the
 * Ordering service once the Catalog service has confirmed that all items are in stock.
 *
 * <p>Simulates payment processing based on the {@code payment.payment-succeeded}
 * configuration property:
 * <ul>
 *   <li>{@code true} (default) — publishes {@link OrderPaymentSucceededIntegrationEvent}</li>
 *   <li>{@code false} — publishes {@link OrderPaymentFailedIntegrationEvent}</li>
 * </ul>
 *
 * <p>This mirrors the .NET {@code OrderStatusChangedToStockConfirmedIntegrationEventHandler}
 * which reads {@code PaymentSettings.PaymentSucceeded} from configuration.
 */
@Slf4j
@Component
public class OrderStatusChangedToStockConfirmedIntegrationEventHandler
        implements IIntegrationEventHandler<OrderStatusChangedToStockConfirmedIntegrationEvent> {

    private final IEventBus eventBus;
    private final boolean paymentSucceeded;

    public OrderStatusChangedToStockConfirmedIntegrationEventHandler(
            IEventBus eventBus,
            @Value("${payment.payment-succeeded:true}") boolean paymentSucceeded) {
        this.eventBus = eventBus;
        this.paymentSucceeded = paymentSucceeded;
    }

    /**
     * Receives an {@link OrderStatusChangedToStockConfirmedIntegrationEvent} from the
     * {@code Payment} queue and publishes a payment outcome event.
     *
     * @param event the inbound integration event carrying the order id
     */
    @RabbitListener(queues = "Payment",
            id = "paymentStockConfirmedListener",
            containerFactory = "rabbitListenerContainerFactory")
    @Override
    public void handle(OrderStatusChangedToStockConfirmedIntegrationEvent event) {
        log.info("Handling integration event: {} - OrderStatusChangedToStockConfirmedIntegrationEvent (orderId={})",
                event.getId(), event.getOrderId());

        int orderId = event.getOrderId();

        if (paymentSucceeded) {
            log.info("Payment simulation succeeded for orderId={} — publishing OrderPaymentSucceededIntegrationEvent",
                    orderId);
            eventBus.publish(new OrderPaymentSucceededIntegrationEvent(orderId));
        } else {
            log.warn("Payment simulation failed for orderId={} — publishing OrderPaymentFailedIntegrationEvent",
                    orderId);
            eventBus.publish(new OrderPaymentFailedIntegrationEvent(orderId));
        }
    }
}
