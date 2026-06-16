package com.eshop.orderinghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Ordering Notification Service.
 *
 * <p>This service is the Java/Spring equivalent of the .NET
 * {@code Ordering.SignalrHub} service. It receives order-status
 * integration events from RabbitMQ and pushes real-time notifications to
 * connected browser clients over WebSocket using the STOMP sub-protocol
 * (with optional SockJS fallback).
 */
@SpringBootApplication
public class OrderingSignalRHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderingSignalRHubApplication.class, args);
    }
}
