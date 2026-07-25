package com.fiap.tech_challenge_fase2.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class PartItem {

    private final String     id;
    private final String     name;
    private final String     partNumber;
    private final int        quantity;
    private final BigDecimal unitPrice;

    public PartItem(String id, String name, String partNumber, int quantity, BigDecimal unitPrice) {
        this.id         = id != null ? id : UUID.randomUUID().toString();
        this.name       = Objects.requireNonNull(name,      "Part name is required");
        this.partNumber = partNumber;
        this.quantity   = quantity;
        this.unitPrice  = Objects.requireNonNull(unitPrice, "Unit price is required");
    }

    public static PartItem create(String name, String partNumber, int quantity, BigDecimal unitPrice) {
        return new PartItem(UUID.randomUUID().toString(), name, partNumber, quantity, unitPrice);
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String     getId()         { return id; }
    public String     getName()       { return name; }
    public String     getPartNumber() { return partNumber; }
    public int        getQuantity()   { return quantity; }
    public BigDecimal getUnitPrice()  { return unitPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartItem p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
