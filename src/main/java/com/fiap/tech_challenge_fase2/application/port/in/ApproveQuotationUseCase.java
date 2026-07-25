package com.fiap.tech_challenge_fase2.application.port.in;

import com.fiap.tech_challenge_fase2.application.dto.ApproveQuotationCommand;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

public interface ApproveQuotationUseCase {
    ServiceOrder execute(ApproveQuotationCommand command);
}
