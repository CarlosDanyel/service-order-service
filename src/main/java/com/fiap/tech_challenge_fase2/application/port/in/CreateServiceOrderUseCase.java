package com.fiap.tech_challenge_fase2.application.port.in;

import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

public interface CreateServiceOrderUseCase {
    ServiceOrder execute(CreateServiceOrderCommand command);
}
