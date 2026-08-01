package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.application.port.in.UpdateServiceOrderStatusUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.RabbitMQConfig;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;

public class UpdateServiceOrderStatusUseCaseImpl implements UpdateServiceOrderStatusUseCase {

    private final ServiceOrderRepositoryPort repository;
    private final EventPublisher eventPublisher;

    public UpdateServiceOrderStatusUseCaseImpl(ServiceOrderRepositoryPort repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ServiceOrder execute(UpdateStatusCommand command) {
        ServiceOrder serviceOrder = repository.findById(command.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(command.serviceOrderId()));

        if (command.services() != null || command.parts() != null) {
            serviceOrder.updateItems(command.services(), command.parts());
        }

        serviceOrder.transitionTo(command.newStatus());

        if (command.newStatus() == ServiceOrderStatus.AWAITING_APPROVAL) {
            serviceOrder.generateApprovalToken();
            ServiceOrder saved = repository.save(serviceOrder);
            String vehicleInfo = saved.getVehicle().getBrand() + " " + saved.getVehicle().getModel() + " (" + saved.getVehicle().getYear() + ") - Placa: " + saved.getVehicle().getLicensePlate();
            eventPublisher.publishEvent(
                    RabbitMQConfig.ROUTING_KEY_QUOTATION,
                    new ServiceOrderEvents.QuotationCreatedEvent(
                            saved.getId(),
                            saved.getOrderNumber(),
                            saved.getCustomer().getName(),
                            saved.getCustomer().getEmail(),
                            vehicleInfo,
                            saved.calculateTotalAmount(),
                            saved.getApprovalToken()
                    )
            );
            return saved;
        }

        return repository.save(serviceOrder);
    }
}
