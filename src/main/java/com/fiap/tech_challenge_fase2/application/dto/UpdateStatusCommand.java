package com.fiap.tech_challenge_fase2.application.dto;

import com.fiap.tech_challenge_fase2.domain.entity.PartItem;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceItem;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;

import java.util.List;

public record UpdateStatusCommand(
        String             serviceOrderId,
        ServiceOrderStatus newStatus,
        List<ServiceItem>  services,
        List<PartItem>     parts
) {
    public UpdateStatusCommand(String serviceOrderId, ServiceOrderStatus newStatus) {
        this(serviceOrderId, newStatus, null, null);
    }
}
