package com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter;

import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.*;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.ServiceOrderJpaRepository;

import java.util.List;
import java.util.Optional;

public class ServiceOrderPersistenceAdapter implements ServiceOrderRepositoryPort {

    private final ServiceOrderJpaRepository jpaRepository;

    public ServiceOrderPersistenceAdapter(ServiceOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ServiceOrder save(ServiceOrder domain) {
        ServiceOrderJpaEntity entity = toJpaEntity(domain);
        return toDomainEntity(jpaRepository.save(entity));
    }

    @Override
    public Optional<ServiceOrder> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public Optional<ServiceOrder> findByApprovalToken(String token) {
        return jpaRepository.findByApprovalToken(token).map(this::toDomainEntity);
    }

    @Override
    public List<ServiceOrder> findActiveOrdered() {
        List<ServiceOrderStatus> excluded = List.of(
                ServiceOrderStatus.FINISHED, ServiceOrderStatus.DELIVERED);
        return jpaRepository.findActiveOrdered(excluded)
                .stream()
                .map(this::toDomainEntity)
                .toList();
    }

    private ServiceOrderJpaEntity toJpaEntity(ServiceOrder d) {
        CustomerJpaEntity customerJpa = CustomerJpaEntity.builder()
                .id(d.getCustomer().getId())
                .name(d.getCustomer().getName())
                .email(d.getCustomer().getEmail())
                .phone(d.getCustomer().getPhone())
                .build();

        VehicleJpaEntity vehicleJpa = VehicleJpaEntity.builder()
                .id(d.getVehicle().getId())
                .licensePlate(d.getVehicle().getLicensePlate())
                .brand(d.getVehicle().getBrand())
                .model(d.getVehicle().getModel())
                .year(d.getVehicle().getYear())
                .color(d.getVehicle().getColor())
                .build();

        ServiceOrderJpaEntity entity = ServiceOrderJpaEntity.builder()
                .id(d.getId())
                .orderNumber(d.getOrderNumber())
                .status(d.getStatus())
                .customer(customerJpa)
                .vehicle(vehicleJpa)
                .approvalToken(d.getApprovalToken())
                .notes(d.getNotes())
                .deleted(d.isDeleted())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();

        List<ServiceItemJpaEntity> serviceItems = d.getServices().stream()
                .map(s -> ServiceItemJpaEntity.builder()
                        .id(s.getId())
                        .serviceOrder(entity)
                        .name(s.getName())
                        .description(s.getDescription())
                        .price(s.getPrice())
                        .estimatedHours(s.getEstimatedHours())
                        .build())
                .toList();

        List<PartItemJpaEntity> partItems = d.getParts().stream()
                .map(p -> PartItemJpaEntity.builder()
                        .id(p.getId())
                        .serviceOrder(entity)
                        .name(p.getName())
                        .partNumber(p.getPartNumber())
                        .quantity(p.getQuantity())
                        .unitPrice(p.getUnitPrice())
                        .build())
                .toList();

        entity.getServices().clear();
        entity.getServices().addAll(serviceItems);
        entity.getParts().clear();
        entity.getParts().addAll(partItems);

        return entity;
    }

    private ServiceOrder toDomainEntity(ServiceOrderJpaEntity e) {
        Customer customer = new Customer(
                e.getCustomer().getId(), e.getCustomer().getName(),
                e.getCustomer().getEmail(), e.getCustomer().getPhone());

        Vehicle vehicle = new Vehicle(
                e.getVehicle().getId(), e.getVehicle().getLicensePlate(),
                e.getVehicle().getBrand(), e.getVehicle().getModel(),
                e.getVehicle().getYear(), e.getVehicle().getColor());

        List<ServiceItem> services = e.getServices().stream()
                .map(s -> new ServiceItem(s.getId(), s.getName(), s.getDescription(),
                        s.getPrice(), s.getEstimatedHours()))
                .toList();

        List<PartItem> parts = e.getParts().stream()
                .map(p -> new PartItem(p.getId(), p.getName(), p.getPartNumber(),
                        p.getQuantity(), p.getUnitPrice()))
                .toList();

        return new ServiceOrder(
                e.getId(), e.getOrderNumber(), e.getStatus(),
                customer, vehicle, services, parts,
                e.getApprovalToken(), e.getNotes(), e.isDeleted(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
