package com.fiap.tech_challenge_fase2.domain.entity;

import java.util.Objects;
import java.util.UUID;

public class Customer {

    private final String id;
    private final String name;
    private final String email;
    private final String phone;

    public Customer(String id, String name, String email, String phone) {
        this.id    = id != null ? id : UUID.randomUUID().toString();
        this.name  = Objects.requireNonNull(name,  "Customer name is required");
        this.email = Objects.requireNonNull(email, "Customer email is required");
        this.phone = phone;
    }

    public static Customer create(String name, String email, String phone) {
        return new Customer(UUID.randomUUID().toString(), name, email, phone);
    }

    public String getId()    { return id; }
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer c)) return false;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
