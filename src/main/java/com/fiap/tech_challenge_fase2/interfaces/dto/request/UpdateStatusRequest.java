package com.fiap.tech_challenge_fase2.interfaces.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Novo status é obrigatório")
        @JsonAlias({"status", "newStatus"})
        ServiceOrderStatus newStatus
) {}
