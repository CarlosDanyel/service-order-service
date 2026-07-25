package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.application.port.in.CreateServiceOrderUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.RabbitMQConfig;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;

import java.util.List;

public class CreateServiceOrderUseCaseImpl implements CreateServiceOrderUseCase {

    private final ServiceOrderRepositoryPort repository;
    private final EventPublisher eventPublisher;

    public CreateServiceOrderUseCaseImpl(ServiceOrderRepositoryPort repository, EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ServiceOrder execute(CreateServiceOrderCommand command) {
        Customer           customer = buildCustomer(command.customer());
        Vehicle            vehicle  = buildVehicle(command.vehicle());
        List<ServiceItem>  services = buildServices(command.services());
        List<PartItem>     parts    = buildParts(command.parts());

        ServiceOrder serviceOrder = ServiceOrder.open(customer, vehicle, services, parts, command.notes());
        ServiceOrder saved = repository.save(serviceOrder);

        String vehicleInfo = saved.getVehicle().getBrand() + " " + saved.getVehicle().getModel() + " (" + saved.getVehicle().getYear() + ") - Placa: " + saved.getVehicle().getLicensePlate();
        eventPublisher.publishEvent(
                RabbitMQConfig.ROUTING_KEY_OS_CREATED,
                new ServiceOrderEvents.ServiceOrderCreatedEvent(
                        saved.getId(),
                        saved.getOrderNumber(),
                        saved.getCustomer().getName(),
                        saved.getCustomer().getEmail(),
                        vehicleInfo,
                        saved.getStatus().name()
                )
        );

        return saved;
    }

    private Customer buildCustomer(CreateServiceOrderCommand.CustomerData d) {
        return Customer.create(d.name(), d.email(), d.phone());
    }

    private Vehicle buildVehicle(CreateServiceOrderCommand.VehicleData d) {
        return Vehicle.create(d.licensePlate(), d.brand(), d.model(), d.year(), d.color());
    }

    private List<ServiceItem> buildServices(List<CreateServiceOrderCommand.ServiceItemData> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(s -> ServiceItem.create(s.name(), s.description(), s.price(), s.estimatedHours()))
                .toList();
    }

    private List<PartItem> buildParts(List<CreateServiceOrderCommand.PartItemData> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(p -> PartItem.create(p.name(), p.partNumber(), p.quantity(), p.unitPrice()))
                .toList();
    }
}
