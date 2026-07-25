package com.fiap.tech_challenge_fase2.domain.entity;

import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.InvalidStatusTransitionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class ServiceOrder {

    private final String              id;
    private final String              orderNumber;
    private ServiceOrderStatus        status;
    private final Customer            customer;
    private final Vehicle             vehicle;
    private final List<ServiceItem>   services;
    private final List<PartItem>      parts;
    private String                    approvalToken;
    private final String              notes;
    private boolean                   deleted;
    private final LocalDateTime       createdAt;
    private LocalDateTime             updatedAt;

    public ServiceOrder(String id, String orderNumber, ServiceOrderStatus status,
                        Customer customer, Vehicle vehicle,
                        List<ServiceItem> services, List<PartItem> parts,
                        String approvalToken, String notes, boolean deleted,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id            = id != null ? id : UUID.randomUUID().toString();
        this.orderNumber   = Objects.requireNonNull(orderNumber, "Order number is required");
        this.status        = Objects.requireNonNull(status,      "Status is required");
        this.customer      = Objects.requireNonNull(customer,    "Customer is required");
        this.vehicle       = Objects.requireNonNull(vehicle,     "Vehicle is required");
        this.services      = new ArrayList<>(services != null ? services : List.of());
        this.parts         = new ArrayList<>(parts    != null ? parts    : List.of());
        this.approvalToken = approvalToken;
        this.notes         = notes;
        this.deleted       = deleted;
        this.createdAt     = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt     = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static ServiceOrder open(Customer customer, Vehicle vehicle,
                                    List<ServiceItem> services, List<PartItem> parts,
                                    String notes) {
        return new ServiceOrder(
                UUID.randomUUID().toString(),
                generateOrderNumber(),
                ServiceOrderStatus.RECEIVED,
                customer, vehicle, services, parts,
                null, notes, false,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    public void transitionTo(ServiceOrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Transição inválida: %s → %s", this.status, newStatus));
        }
        this.status    = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void generateApprovalToken() {
        this.approvalToken = UUID.randomUUID().toString();
        this.updatedAt     = LocalDateTime.now();
    }

    public void approve(String token) {
        validateToken(token);
        transitionTo(ServiceOrderStatus.EXECUTION);
        clearToken();
    }

    public void refuse(String token) {
        validateToken(token);
        transitionTo(ServiceOrderStatus.DIAGNOSIS);
        clearToken();
    }

    public void softDelete() {
        this.deleted   = true;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotalAmount() {
        BigDecimal servicesTotal = services.stream()
                .map(ServiceItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal partsTotal = parts.stream()
                .map(PartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return servicesTotal.add(partsTotal);
    }

    private void validateToken(String token) {
        if (this.approvalToken == null || !this.approvalToken.equals(token)) {
            throw new InvalidStatusTransitionException("Token de aprovação inválido ou expirado");
        }
        if (this.status != ServiceOrderStatus.AWAITING_APPROVAL) {
            throw new InvalidStatusTransitionException(
                    "OS não está aguardando aprovação. Status atual: " + this.status);
        }
    }

    private void clearToken() {
        this.approvalToken = null;
    }

    private static String generateOrderNumber() {
        return "OS-" + System.currentTimeMillis();
    }

    public String              getId()            { return id; }
    public String              getOrderNumber()   { return orderNumber; }
    public ServiceOrderStatus  getStatus()        { return status; }
    public Customer            getCustomer()      { return customer; }
    public Vehicle             getVehicle()       { return vehicle; }
    public List<ServiceItem>   getServices()      { return Collections.unmodifiableList(services); }
    public List<PartItem>      getParts()         { return Collections.unmodifiableList(parts); }
    public String              getApprovalToken() { return approvalToken; }
    public String              getNotes()         { return notes; }
    public boolean             isDeleted()        { return deleted; }
    public LocalDateTime       getCreatedAt()     { return createdAt; }
    public LocalDateTime       getUpdatedAt()     { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceOrder s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
