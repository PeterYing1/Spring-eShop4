package com.eshop.ordering.domain.aggregatesmodel.buyer;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card type lookup entity.
 *
 * <p>Mapped to the {@code ordering.cardtypes} table which is seeded with fixed
 * values (1=Amex, 2=Visa, 3=MasterCard). The PK is NOT auto-generated.
 */
@jakarta.persistence.Entity
@Table(name = "cardtypes", schema = "ordering")
public class CardType {

    @Id
    @Column(name = "Id", nullable = false)
    private int id;

    @Column(name = "Name", nullable = false, length = 200)
    private String name;

    /** Required by JPA. */
    protected CardType() {
    }

    public CardType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
