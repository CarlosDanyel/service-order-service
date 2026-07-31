package com.fiap.tech_challenge_fase2.interfaces.controller;

import com.fiap.tech_challenge_fase2.application.port.in.CreateServiceOrderUseCase;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.CreateServiceOrderRequest;
import com.fiap.tech_challenge_fase2.interfaces.dto.response.ServiceOrderResponse;
import com.fiap.tech_challenge_fase2.interfaces.mapper.ServiceOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-orders")
@Tag(name = "1. Abertura de OS", description = "Endpoint para abertura de novas Ordens de Serviço")
public class ServiceOrderOpeningController {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final ServiceOrderMapper        mapper;

    public ServiceOrderOpeningController(CreateServiceOrderUseCase createServiceOrderUseCase,
                                          ServiceOrderMapper mapper) {
        this.createServiceOrderUseCase = createServiceOrderUseCase;
        this.mapper                    = mapper;
    }

    @PostMapping
    @Operation(
        summary     = "Abre uma nova Ordem de Serviço",
        description = """
                Recebe os dados do cliente, veículo, serviços e peças.
                Retorna a OS com ID único. Status inicial: RECEIVED (Recebida).
                Um e-mail de confirmação é enviado ao cliente automaticamente.
                """
    )
    public ResponseEntity<ServiceOrderResponse> openServiceOrder(
            @Valid @RequestBody CreateServiceOrderRequest request) {

        ServiceOrder serviceOrder = createServiceOrderUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(serviceOrder));
    }
}
