package com.eshop.ordering.application.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Command to cancel an existing order.
 *
 * <p>Wrapped in an {@code IdentifiedCommand} at the controller layer to provide
 * idempotency based on the {@code x-requestid} header.
 */
public class CancelOrderCommand {

    @NotNull
    @Min(1)
    @JsonProperty("orderNumber")
    private int orderNumber;

    public CancelOrderCommand() {
    }

    public CancelOrderCommand(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
