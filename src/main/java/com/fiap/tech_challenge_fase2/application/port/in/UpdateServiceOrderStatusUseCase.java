package com.fiap.tech_challenge_fase2.application.port.in;

import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

public interface UpdateServiceOrderStatusUseCase {
    ServiceOrder execute(UpdateStatusCommand command);
}
