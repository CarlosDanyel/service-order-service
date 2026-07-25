package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.application.port.in.UpdateServiceOrderStatusUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.EmailNotificationGateway;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;

public class UpdateServiceOrderStatusUseCaseImpl implements UpdateServiceOrderStatusUseCase {

    private final ServiceOrderRepositoryPort repository;
    private final EmailNotificationGateway   emailGateway;

    public UpdateServiceOrderStatusUseCaseImpl(ServiceOrderRepositoryPort repository,
                                                EmailNotificationGateway emailGateway) {
        this.repository   = repository;
        this.emailGateway = emailGateway;
    }

    @Override
    public ServiceOrder execute(UpdateStatusCommand command) {
        ServiceOrder serviceOrder = repository.findById(command.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(command.serviceOrderId()));

        serviceOrder.transitionTo(command.newStatus());

        if (command.newStatus() == ServiceOrderStatus.AWAITING_APPROVAL) {
            serviceOrder.generateApprovalToken();
            ServiceOrder saved = repository.save(serviceOrder);
            emailGateway.sendQuotationApprovalEmail(saved);
            return saved;
        }

        ServiceOrder saved = repository.save(serviceOrder);
        emailGateway.sendStatusUpdateEmail(saved);
        return saved;
    }
}
