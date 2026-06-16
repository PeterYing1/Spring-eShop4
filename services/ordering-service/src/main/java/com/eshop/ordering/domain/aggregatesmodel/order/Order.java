package com.eshop.ordering.domain.aggregatesmodel.order;

import com.eshop.ordering.domain.events.OrderCancelledDomainEvent;
import com.eshop.ordering.domain.events.OrderShippedDomainEvent;
import com.eshop.ordering.domain.events.OrderStartedDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToAwaitingValidationDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToPaidDomainEvent;
import com.eshop.ordering.domain.events.OrderStatusChangedToStockConfirmedDomainEvent;
import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import com.eshop.ordering.domain.seedwork.Entity;
import com.eshop.ordering.domain.seedwork.IAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order aggregate root.
 *
 * <p>Encapsulates all order lifecycle state transitions. Business rules:
 * <ul>
 *   <li>Status may only move forward along the valid transition chain.</li>
 *   <li>Cancellation is forbidden when the order is PAID or SHIPPED.</li>
 *   <li>Order items are managed exclusively through {@link #addOrderItem}.</li>
 * </ul>
 */
@jakarta.persistence.Entity
@Table(name = "orders", schema = "ordering")
public class Order extends Entity implements IAggregateRoot {

    @Column(name = "OrderDate", nullable = false)
    private Instant orderDate;

    @Embedded
    private Address address;

    @Column(name = "BuyerId")
    private Integer buyerId;

    @Column(name = "OrderStatusId", nullable = false)
    private int orderStatusId;

    @Column(name = "Description")
    private String description;

    /** Draft orders are not persisted — only used for the draft creation flow. */
    @Transient
    private boolean isDraft;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "OrderId")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "PaymentMethodId")
    private Integer paymentMethodId;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Required by JPA. */
    protected Order() {
        isDraft = false;
    }

    /**
     * Creates a new submitted order and adds the {@link OrderStartedDomainEvent}.
     *
     * @param userId              identity subject of the user
     * @param userName            display name of the user
     * @param address             shipping address
     * @param cardTypeId          card type (1=Amex, 2=Visa, 3=MasterCard)
     * @param cardNumber          payment card number
     * @param cardSecurityNumber  CVV
     * @param cardHolderName      name on the card
     * @param cardExpiration      card expiry
     * @param buyerId             optional buyer id (may be set later by domain event handler)
     * @param paymentMethodId     optional payment method id (may be set later)
     */
    public Order(String userId, String userName, Address address,
                 int cardTypeId, String cardNumber, String cardSecurityNumber,
                 String cardHolderName, Instant cardExpiration,
                 Integer buyerId, Integer paymentMethodId) {
        this();
        this.buyerId = buyerId;
        this.paymentMethodId = paymentMethodId;
        this.orderStatusId = OrderStatus.SUBMITTED.getId();
        this.orderDate = Instant.now();
        this.address = address;

        addDomainEvent(new OrderStartedDomainEvent(
                this, userId, userName,
                cardTypeId, cardNumber, cardSecurityNumber, cardHolderName, cardExpiration));
    }

    // -------------------------------------------------------------------------
    // Static factory
    // -------------------------------------------------------------------------

    /**
     * Creates a draft order (not persisted).
     *
     * <p>Used by {@code CreateOrderDraftCommandHandler} to build a transient
     * order purely for calculating totals and returning the draft DTO.
     */
    public static Order newDraft() {
        Order order = new Order();
        order.isDraft = true;
        return order;
    }

    // -------------------------------------------------------------------------
    // Order item management
    // -------------------------------------------------------------------------

    /**
     * Adds a product to the order.
     *
     * <p>If the product already exists as a line item the discount is updated
     * (only if higher) and the units are incremented. Otherwise a new
     * {@link OrderItem} is appended.
     */
    public void addOrderItem(int productId, String productName, BigDecimal unitPrice,
                             BigDecimal discount, String pictureUrl, int units) {
        OrderItem existing = orderItems.stream()
                .filter(i -> i.getProductId() == productId)
                .findFirst()
                .orElse(null);

        if (existing != null) {
            if (discount.compareTo(existing.getCurrentDiscount()) > 0) {
                existing.setNewDiscount(discount);
            }
            existing.addUnits(units);
        } else {
            orderItems.add(new OrderItem(productId, productName, unitPrice, discount, pictureUrl, units));
        }
    }

    // -------------------------------------------------------------------------
    // Setters called by domain event handlers
    // -------------------------------------------------------------------------

    public void setPaymentId(int id) {
        this.paymentMethodId = id;
    }

    public void setBuyerId(int id) {
        this.buyerId = id;
    }

    // -------------------------------------------------------------------------
    // Status transitions
    // -------------------------------------------------------------------------

    /**
     * Transitions to AWAITING_VALIDATION.
     * Only allowed from SUBMITTED.
     */
    public void setAwaitingValidationStatus() {
        if (orderStatusId == OrderStatus.SUBMITTED.getId()) {
            addDomainEvent(new OrderStatusChangedToAwaitingValidationDomainEvent(
                    getId() != null ? getId() : 0, orderItems));
            orderStatusId = OrderStatus.AWAITING_VALIDATION.getId();
        }
    }

    /**
     * Transitions to STOCK_CONFIRMED.
     * Only allowed from AWAITING_VALIDATION.
     */
    public void setStockConfirmedStatus() {
        if (orderStatusId == OrderStatus.AWAITING_VALIDATION.getId()) {
            addDomainEvent(new OrderStatusChangedToStockConfirmedDomainEvent(
                    getId() != null ? getId() : 0));
            orderStatusId = OrderStatus.STOCK_CONFIRMED.getId();
            description = "All the items were confirmed with available stock.";
        }
    }

    /**
     * Transitions to PAID.
     * Only allowed from STOCK_CONFIRMED.
     */
    public void setPaidStatus() {
        if (orderStatusId == OrderStatus.STOCK_CONFIRMED.getId()) {
            addDomainEvent(new OrderStatusChangedToPaidDomainEvent(
                    getId() != null ? getId() : 0, orderItems));
            orderStatusId = OrderStatus.PAID.getId();
            description = "The payment was performed at a simulated \"American Bank checking bank account ending on XX35071\"";
        }
    }

    /**
     * Transitions to SHIPPED.
     * Only allowed from PAID; throws otherwise.
     */
    public void setShippedStatus() {
        if (orderStatusId != OrderStatus.PAID.getId()) {
            statusChangeException(OrderStatus.SHIPPED);
        }
        orderStatusId = OrderStatus.SHIPPED.getId();
        description = "The order was shipped.";
        addDomainEvent(new OrderShippedDomainEvent(this));
    }

    /**
     * Transitions to CANCELLED.
     * Forbidden when already PAID or SHIPPED.
     */
    public void setCancelledStatus() {
        if (orderStatusId == OrderStatus.PAID.getId()
                || orderStatusId == OrderStatus.SHIPPED.getId()) {
            statusChangeException(OrderStatus.CANCELLED);
        }
        orderStatusId = OrderStatus.CANCELLED.getId();
        description = "The order was cancelled.";
        addDomainEvent(new OrderCancelledDomainEvent(this));
    }

    /**
     * Cancels the order because some stock items were rejected.
     * Only allowed from AWAITING_VALIDATION.
     *
     * @param orderStockRejectedItems product ids whose stock was insufficient
     */
    public void setCancelledStatusWhenStockIsRejected(List<Integer> orderStockRejectedItems) {
        if (orderStatusId == OrderStatus.AWAITING_VALIDATION.getId()) {
            orderStatusId = OrderStatus.CANCELLED.getId();
            String rejectedNames = orderItems.stream()
                    .filter(i -> orderStockRejectedItems.contains(i.getProductId()))
                    .map(OrderItem::getOrderItemProductName)
                    .collect(Collectors.joining(", "));
            description = "The product items don't have stock: (" + rejectedNames + ").";
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Calculates the order total as the sum of {@code units × unitPrice}
     * across all order items (before discount).
     */
    public BigDecimal getTotal() {
        return orderItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getUnits())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Returns the resolved {@link OrderStatus} for the current status id. */
    public OrderStatus getOrderStatus() {
        return OrderStatus.from(orderStatusId);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Instant getOrderDate() { return orderDate; }
    public Address getAddress() { return address; }
    public Integer getBuyerId() { return buyerId; }
    public Integer getPaymentMethodId() { return paymentMethodId; }
    public String getDescription() { return description; }
    public boolean isDraft() { return isDraft; }
    public List<OrderItem> getOrderItems() { return Collections.unmodifiableList(orderItems); }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void statusChangeException(OrderStatus orderStatusToChange) {
        throw new OrderingDomainException(
                "Is not possible to change the order status from "
                + getOrderStatus().getName()
                + " to "
                + orderStatusToChange.getName() + ".");
    }
}
