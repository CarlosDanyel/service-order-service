package com.fiap.tech_challenge_fase2.interfaces.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceOrderResponse(
        String id,
        String orderNumber,
        String status,
        String statusDescription,
        CustomerResponse customer,
        VehicleResponse vehicle,
        List<ServiceItemResponse> services,
        List<PartItemResponse> parts,
        BigDecimal totalAmount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record CustomerResponse(String id, String name, String email, String phone) {}

    public record VehicleResponse(String id, String licensePlate,
                                   String brand, String model, int year, String color) {}

    public record ServiceItemResponse(String id, String name, String description,
                                       BigDecimal price, Double estimatedHours) {}

    public record PartItemResponse(String id, String name, String partNumber,
                                    int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {}
}
