package com.eshop.ordering.application.queries;

/**
 * Query-side DTO for a card type.
 *
 * <p>Returned by {@code GET /api/v1/orders/cardtypes}.
 * Distinct from the domain entity {@link com.eshop.ordering.domain.aggregatesmodel.buyer.CardType}.
 */
public class CardType {

    private int id;
    private String name;

    public CardType() {
    }

    public CardType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
