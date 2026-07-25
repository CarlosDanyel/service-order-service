package com.fiap.tech_challenge_fase2.domain.entity;

import java.util.Objects;
import java.util.UUID;

public class Vehicle {

    private final String id;
    private final String licensePlate;
    private final String brand;
    private final String model;
    private final int    year;
    private final String color;

    public Vehicle(String id, String licensePlate, String brand, String model, int year, String color) {
        this.id           = id != null ? id : UUID.randomUUID().toString();
        this.licensePlate = Objects.requireNonNull(licensePlate, "License plate is required");
        this.brand        = Objects.requireNonNull(brand,        "Brand is required");
        this.model        = Objects.requireNonNull(model,        "Model is required");
        this.year         = year;
        this.color        = color;
    }

    public static Vehicle create(String licensePlate, String brand, String model, int year, String color) {
        return new Vehicle(UUID.randomUUID().toString(), licensePlate, brand, model, year, color);
    }

    public String getId()           { return id; }
    public String getLicensePlate() { return licensePlate; }
    public String getBrand()        { return brand; }
    public String getModel()        { return model; }
    public int    getYear()         { return year; }
    public String getColor()        { return color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle v)) return false;
        return Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
