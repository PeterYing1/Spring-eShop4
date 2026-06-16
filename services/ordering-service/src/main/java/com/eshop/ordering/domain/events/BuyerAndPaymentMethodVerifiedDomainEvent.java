package com.eshop.ordering.domain.events;

import com.eshop.ordering.domain.aggregatesmodel.buyer.Buyer;
import com.eshop.ordering.domain.aggregatesmodel.buyer.PaymentMethod;

/**
 * Raised by {@link Buyer#verifyOrAddPaymentMethod} whenever a buyer and
 * payment method have been verified (or created) for an order.
 *
 * <p>Handled by {@code UpdateOrderWhenBuyerAndPaymentMethodVerifiedDomainEventHandler}
 * to set the buyer and payment method identifiers on the order.
 */
public class BuyerAndPaymentMethodVerifiedDomainEvent {

    private final Buyer buyer;
    private final PaymentMethod payment;
    private final int orderId;

    public BuyerAndPaymentMethodVerifiedDomainEvent(Buyer buyer, PaymentMethod payment, int orderId) {
        this.buyer = buyer;
        this.payment = payment;
        this.orderId = orderId;
    }

    public Buyer getBuyer() { return buyer; }
    public PaymentMethod getPayment() { return payment; }
    public int getOrderId() { return orderId; }
}
