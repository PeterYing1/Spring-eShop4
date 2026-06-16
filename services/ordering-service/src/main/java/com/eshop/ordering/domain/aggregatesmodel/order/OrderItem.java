package com.eshop.ordering.domain.aggregatesmodel.order;

import com.eshop.ordering.domain.exceptions.OrderingDomainException;
import com.eshop.ordering.domain.seedwork.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Order line item — child entity of the {@link Order} aggregate root.
 *
 * <p>Business invariants:
 * <ul>
 *   <li>Units must be &gt; 0.</li>
 *   <li>Total ({@code unitPrice × units}) must be &ge; discount.</li>
 *   <li>Discount cannot be negative.</li>
 * </ul>
 */
@jakarta.persistence.Entity
@Table(name = "orderItems", schema = "ordering")
public class OrderItem extends Entity {

    @Column(name = "ProductId", nullable = false)
    private int productId;

    @Column(name = "ProductName", nullable = false)
    private String productName;

    @Column(name = "UnitPrice", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Discount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discount;

    @Column(name = "Units", nullable = false)
    private int units;

    @Column(name = "PictureUrl")
    private String pictureUrl;

    @Column(name = "OrderId", nullable = false)
    private int orderId;

    /** Required by JPA. */
    protected OrderItem() {
    }

    /**
     * Creates a new order item with full validation.
     *
     * @param productId   product identifier
     * @param productName product name
     * @param unitPrice   price per unit
     * @param discount    discount applied to this line
     * @param pictureUrl  picture URL (may be null)
     * @param units       number of units; must be &gt; 0
     * @throws OrderingDomainException if units &le; 0 or total &lt; discount
     */
    public OrderItem(int productId, String productName, BigDecimal unitPrice,
                     BigDecimal discount, String pictureUrl, int units) {
        if (units <= 0) {
            throw new OrderingDomainException("Invalid number of units");
        }
        if (unitPrice.multiply(BigDecimal.valueOf(units)).compareTo(discount) < 0) {
            throw new OrderingDomainException("The total of order item is lower than applied discount");
        }

        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.pictureUrl = pictureUrl;
        this.units = units;
    }

    // -------------------------------------------------------------------------
    // Read accessors
    // -------------------------------------------------------------------------

    public int getProductId() {
        return productId;
    }

    public String getPictureUri() {
        return pictureUrl;
    }

    public BigDecimal getCurrentDiscount() {
        return discount;
    }

    public int getUnits() {
        return units;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getOrderItemProductName() {
        return productName;
    }

    public int getOrderId() {
        return orderId;
    }

    // -------------------------------------------------------------------------
    // Mutators with domain validation
    // -------------------------------------------------------------------------

    /**
     * Sets a new discount for this item.
     *
     * @param discount must be &ge; 0
     * @throws OrderingDomainException if negative
     */
    public void setNewDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderingDomainException("Discount is not valid");
        }
        this.discount = discount;
    }

    /**
     * Adds units to the current quantity.
     *
     * @param units must be &ge; 0
     * @throws OrderingDomainException if negative
     */
    public void addUnits(int units) {
        if (units < 0) {
            throw new OrderingDomainException("Invalid units");
        }
        this.units += units;
    }
}
