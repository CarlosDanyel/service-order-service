package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.dto.ApproveQuotationCommand;
import com.fiap.tech_challenge_fase2.application.port.in.ApproveQuotationUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;

public class ApproveQuotationUseCaseImpl implements ApproveQuotationUseCase {

    private final ServiceOrderRepositoryPort repository;

    public ApproveQuotationUseCaseImpl(ServiceOrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrder execute(ApproveQuotationCommand command) {
        ServiceOrder serviceOrder = repository.findByApprovalToken(command.token())
                .orElseThrow(() -> new ServiceOrderNotFoundException(command.serviceOrderId()));

        if (command.approved()) {
            serviceOrder.approve(command.token());
        } else {
            serviceOrder.refuse(command.token());
        }

        return repository.save(serviceOrder);
    }
}
