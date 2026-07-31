package com.fiap.tech_challenge_fase2.interfaces.mapper;

import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.CreateServiceOrderRequest;
import com.fiap.tech_challenge_fase2.interfaces.dto.response.ServiceOrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceOrderMapper {

    public CreateServiceOrderCommand toCommand(CreateServiceOrderRequest req) {
        return new CreateServiceOrderCommand(
                new CreateServiceOrderCommand.CustomerData(
                        req.customer().name(), req.customer().email(), req.customer().phone()),
                new CreateServiceOrderCommand.VehicleData(
                        req.vehicle().licensePlate(), req.vehicle().brand(),
                        req.vehicle().model(), req.vehicle().year(), req.vehicle().color()),
                toServiceItemsCommand(req.services()),
                toPartItemsCommand(req.parts()),
                req.notes()
        );
    }

    public ServiceOrderResponse toResponse(ServiceOrder domain) {
        return new ServiceOrderResponse(
                domain.getId(),
                domain.getOrderNumber(),
                domain.getStatus().name(),
                domain.getStatus().getDescription(),
                domain.getApprovalToken(),
                new ServiceOrderResponse.CustomerResponse(
                        domain.getCustomer().getId(), domain.getCustomer().getName(),
                        domain.getCustomer().getEmail(), domain.getCustomer().getPhone()),
                new ServiceOrderResponse.VehicleResponse(
                        domain.getVehicle().getId(), domain.getVehicle().getLicensePlate(),
                        domain.getVehicle().getBrand(), domain.getVehicle().getModel(),
                        domain.getVehicle().getYear(), domain.getVehicle().getColor()),
                domain.getServices().stream()
                        .map(s -> new ServiceOrderResponse.ServiceItemResponse(
                                s.getId(), s.getName(), s.getDescription(),
                                s.getPrice(), s.getEstimatedHours()))
                        .toList(),
                domain.getParts().stream()
                        .map(p -> new ServiceOrderResponse.PartItemResponse(
                                p.getId(), p.getName(), p.getPartNumber(),
                                p.getQuantity(), p.getUnitPrice(), p.getTotalPrice()))
                        .toList(),
                domain.calculateTotalAmount(),
                domain.getNotes(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public List<ServiceOrderResponse> toResponseList(List<ServiceOrder> domains) {
        return domains.stream().map(this::toResponse).toList();
    }

    private List<CreateServiceOrderCommand.ServiceItemData> toServiceItemsCommand(
            List<CreateServiceOrderRequest.ServiceItemRequest> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(s -> new CreateServiceOrderCommand.ServiceItemData(
                        s.name(), s.description(), s.price(), s.estimatedHours()))
                .toList();
    }

    private List<CreateServiceOrderCommand.PartItemData> toPartItemsCommand(
            List<CreateServiceOrderRequest.PartItemRequest> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(p -> new CreateServiceOrderCommand.PartItemData(
                        p.name(), p.partNumber(), p.quantity(), p.unitPrice()))
                .toList();
    }
}
