package com.fiap.tech_challenge_fase2.interfaces.dto.request;

import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Novo status é obrigatório")
        ServiceOrderStatus newStatus
) {}
