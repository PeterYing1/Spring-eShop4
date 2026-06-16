package com.eshop.basket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Basket microservice.
 *
 * <p>Provides a Redis-backed shopping cart with checkout integration via
 * RabbitMQ integration events.
 */
@SpringBootApplication
public class BasketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasketServiceApplication.class, args);
    }
}
