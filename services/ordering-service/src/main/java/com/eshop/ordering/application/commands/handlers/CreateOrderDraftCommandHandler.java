package com.eshop.ordering.application.commands.handlers;

import com.eshop.ordering.application.commands.CreateOrderDraftCommand;
import com.eshop.ordering.application.queries.OrderDraftDTO;
import com.eshop.ordering.domain.aggregatesmodel.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles {@link CreateOrderDraftCommand}.
 *
 * <p>Builds a transient draft order from basket items without persisting it,
 * then maps the result to an {@link OrderDraftDTO} for the client.
 */
@Service
public class CreateOrderDraftCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderDraftCommandHandler.class);

    public CreateOrderDraftCommandHandler() {
    }

    /**
     * Creates a draft order from the basket items and returns the DTO.
     *
     * @param command the draft command
     * @return the populated draft DTO
     */
    public OrderDraftDTO handle(CreateOrderDraftCommand command) {
        log.info("----- Creating order draft for buyer '{}'", command.getBuyerId());

        Order order = Order.newDraft();

        if (command.getItems() != null) {
            for (CreateOrderDraftCommand.BasketItemDTO item : command.getItems()) {
                order.addOrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        BigDecimal.ZERO,
                        item.getPictureUrl(),
                        item.getQuantity());
            }
        }

        List<OrderDraftDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        for (var item : order.getOrderItems()) {
            itemDTOs.add(new OrderDraftDTO.OrderItemDTO(
                    item.getProductId(),
                    item.getOrderItemProductName(),
                    item.getUnitPrice(),
                    item.getCurrentDiscount(),
                    item.getUnits(),
                    item.getPictureUri()));
        }

        return new OrderDraftDTO(itemDTOs, order.getTotal());
    }
}
