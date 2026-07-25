package com.fiap.tech_challenge_fase2.application.dto;

import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;

public record UpdateStatusCommand(
        String             serviceOrderId,
        ServiceOrderStatus newStatus
) {}
