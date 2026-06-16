package com.eshop.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Payment microservice.
 *
 * <p>The Payment service is a purely event-driven component with no REST API
 * and no database.  It:
 * <ol>
 *   <li>Listens for {@code OrderStatusChangedToStockConfirmedIntegrationEvent}
 *       on the RabbitMQ {@code Payment} queue.</li>
 *   <li>Simulates payment processing (configurable via
 *       {@code payment.payment-succeeded} property).</li>
 *   <li>Publishes either {@code OrderPaymentSucceededIntegrationEvent} or
 *       {@code OrderPaymentFailedIntegrationEvent} back to the
 *       {@code eshop_event_bus} exchange.</li>
 * </ol>
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
