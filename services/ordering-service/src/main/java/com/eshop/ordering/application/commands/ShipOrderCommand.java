package com.eshop.ordering.application.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Command to ship a paid order.
 *
 * <p>Wrapped in an {@code IdentifiedCommand} at the controller layer.
 */
public class ShipOrderCommand {

    @NotNull
    @Min(1)
    @JsonProperty("orderNumber")
    private int orderNumber;

    public ShipOrderCommand() {
    }

    public ShipOrderCommand(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
