package com.fiap.tech_challenge_fase2.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateServiceOrderCommand(
        CustomerData  customer,
        VehicleData   vehicle,
        List<ServiceItemData> services,
        List<PartItemData>    parts,
        String notes
) {
    public record CustomerData(String name, String email, String phone) {}

    public record VehicleData(String licensePlate, String brand,
                               String model, int year, String color) {}

    public record ServiceItemData(String name, String description,
                                   BigDecimal price, Double estimatedHours) {}

    public record PartItemData(String name, String partNumber,
                                int quantity, BigDecimal unitPrice) {}
}
