package com.fiap.tech_challenge_fase2.interfaces.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateStatusRequest(
        @NotNull(message = "Novo status é obrigatório")
        @JsonAlias({"status", "newStatus"})
        ServiceOrderStatus newStatus,

        @Valid
        List<CreateServiceOrderRequest.ServiceItemRequest> services,

        @Valid
        List<CreateServiceOrderRequest.PartItemRequest> parts
) {
    public UpdateStatusRequest(ServiceOrderStatus newStatus) {
        this(newStatus, null, null);
    }
}
