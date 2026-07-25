package com.fiap.tech_challenge_fase2.interfaces.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateServiceOrderRequest(

        @Valid @NotNull(message = "Dados do cliente são obrigatórios")
        CustomerRequest customer,

        @Valid @NotNull(message = "Dados do veículo são obrigatórios")
        VehicleRequest vehicle,

        @Valid
        List<ServiceItemRequest> services,

        @Valid
        List<PartItemRequest> parts,

        String notes

) {

    public record CustomerRequest(

            @NotBlank(message = "Nome do cliente é obrigatório")
            String name,

            @NotBlank(message = "E-mail do cliente é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            String phone
    ) {}

    public record VehicleRequest(

            @NotBlank(message = "Placa do veículo é obrigatória")
            String licensePlate,

            @NotBlank(message = "Marca é obrigatória")
            String brand,

            @NotBlank(message = "Modelo é obrigatório")
            String model,

            @Min(value = 1900, message = "Ano inválido")
            int year,

            String color
    ) {}

    public record ServiceItemRequest(

            @NotBlank(message = "Nome do serviço é obrigatório")
            String name,

            String description,

            @NotNull(message = "Preço do serviço é obrigatório")
            @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
            BigDecimal price,

            Double estimatedHours
    ) {}

    public record PartItemRequest(

            @NotBlank(message = "Nome da peça é obrigatório")
            String name,

            String partNumber,

            @Min(value = 1, message = "Quantidade mínima é 1")
            int quantity,

            @NotNull(message = "Preço unitário é obrigatório")
            @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
            BigDecimal unitPrice
    ) {}
}
