package com.fiap.tech_challenge_fase2.interfaces.controller;

import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.application.port.in.GetServiceOrderStatusUseCase;
import com.fiap.tech_challenge_fase2.application.port.in.ListServiceOrdersUseCase;
import com.fiap.tech_challenge_fase2.application.port.in.UpdateServiceOrderStatusUseCase;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.UpdateStatusRequest;
import com.fiap.tech_challenge_fase2.interfaces.dto.response.ServiceOrderResponse;
import com.fiap.tech_challenge_fase2.interfaces.mapper.ServiceOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-orders")
@Tag(name = "2. Status e Listagem de OS",
     description = "Consulta, listagem e atualização de status das Ordens de Serviço")
public class ServiceOrderStatusController {

    private final GetServiceOrderStatusUseCase    getStatusUseCase;
    private final ListServiceOrdersUseCase        listUseCase;
    private final UpdateServiceOrderStatusUseCase updateStatusUseCase;
    private final ServiceOrderMapper              mapper;

    public ServiceOrderStatusController(
            GetServiceOrderStatusUseCase getStatusUseCase,
            ListServiceOrdersUseCase listUseCase,
            UpdateServiceOrderStatusUseCase updateStatusUseCase,
            ServiceOrderMapper mapper) {
        this.getStatusUseCase    = getStatusUseCase;
        this.listUseCase         = listUseCase;
        this.updateStatusUseCase = updateStatusUseCase;
        this.mapper              = mapper;
    }

    @GetMapping("/{id}/status")
    @Operation(
        summary     = "Consulta o status atual de uma OS",
        description = """
                Retorna a situação atual da OS:
                RECEIVED (Recebida), DIAGNOSIS (Diagnóstico),
                AWAITING_APPROVAL (Aguardando Aprovação), EXECUTION (Em Execução),
                FINISHED (Finalizada), DELIVERED (Entregue).
                """
    )
    public ResponseEntity<ServiceOrderResponse> getStatus(
            @Parameter(description = "ID único da OS") @PathVariable String id) {

        ServiceOrder serviceOrder = getStatusUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(serviceOrder));
    }

    @GetMapping
    @Operation(
        summary     = "Lista OS ativas ordenadas por prioridade",
        description = """
                Retorna as OS ativas ordenadas por status:
                Em Execução > Aguardando Aprovação > Diagnóstico > Recebida.
                Dentro do mesmo status: mais antigas primeiro.
                OS Finalizadas e Entregues são excluídas (soft-delete também).
                """
    )
    public ResponseEntity<List<ServiceOrderResponse>> listServiceOrders() {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute()));
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary     = "Atualiza o status de uma OS",
        description = """
                Atualiza o status da OS seguindo as regras de transição:
                RECEIVED → DIAGNOSIS → AWAITING_APPROVAL → EXECUTION → FINISHED → DELIVERED.
                Ao entrar em AWAITING_APPROVAL, envia e-mail com botões Aprovar/Recusar.
                """
    )
    public ResponseEntity<ServiceOrderResponse> updateStatus(
            @Parameter(description = "ID único da OS") @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request) {

        UpdateStatusCommand command      = mapper.toUpdateStatusCommand(id, request);
        ServiceOrder        serviceOrder = updateStatusUseCase.execute(command);
        return ResponseEntity.ok(mapper.toResponse(serviceOrder));
    }
}
