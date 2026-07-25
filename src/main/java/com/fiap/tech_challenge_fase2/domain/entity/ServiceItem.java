package com.fiap.tech_challenge_fase2.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class ServiceItem {

    private final String     id;
    private final String     name;
    private final String     description;
    private final BigDecimal price;
    private final Double     estimatedHours;

    public ServiceItem(String id, String name, String description, BigDecimal price, Double estimatedHours) {
        this.id             = id != null ? id : UUID.randomUUID().toString();
        this.name           = Objects.requireNonNull(name,  "Service name is required");
        this.description    = description;
        this.price          = Objects.requireNonNull(price, "Service price is required");
        this.estimatedHours = estimatedHours;
    }

    public static ServiceItem create(String name, String description, BigDecimal price, Double estimatedHours) {
        return new ServiceItem(UUID.randomUUID().toString(), name, description, price, estimatedHours);
    }

    public String     getId()             { return id; }
    public String     getName()           { return name; }
    public String     getDescription()    { return description; }
    public BigDecimal getPrice()          { return price; }
    public Double     getEstimatedHours() { return estimatedHours; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceItem s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
