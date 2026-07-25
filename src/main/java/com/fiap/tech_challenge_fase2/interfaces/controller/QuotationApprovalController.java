package com.fiap.tech_challenge_fase2.interfaces.controller;

import com.fiap.tech_challenge_fase2.application.dto.ApproveQuotationCommand;
import com.fiap.tech_challenge_fase2.application.port.in.ApproveQuotationUseCase;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.interfaces.dto.response.ServiceOrderResponse;
import com.fiap.tech_challenge_fase2.interfaces.mapper.ServiceOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quotations")
@Tag(name = "3. Aprovação de Orçamento",
     description = "Endpoint para aprovação ou recusa de orçamento pelo cliente via link de e-mail")
public class QuotationApprovalController {

    private final ApproveQuotationUseCase approveQuotationUseCase;
    private final ServiceOrderMapper      mapper;

    public QuotationApprovalController(ApproveQuotationUseCase approveQuotationUseCase,
                                        ServiceOrderMapper mapper) {
        this.approveQuotationUseCase = approveQuotationUseCase;
        this.mapper                  = mapper;
    }

    @GetMapping("/{id}")
    @Operation(
        summary     = "Processa aprovação ou recusa do orçamento",
        description = """
                Endpoint chamado pelos links do e-mail enviado ao cliente.
                Valida o token de segurança e aplica a decisão:
                  - approved=true  → OS avança para Em Execução
                  - approved=false → OS retorna para Diagnóstico
                O token é invalidado após o uso (one-time use).
                """
    )
    public ResponseEntity<ServiceOrderResponse> processDecision(
            @Parameter(description = "ID único da OS")              @PathVariable String id,
            @Parameter(description = "Token de aprovação do e-mail") @RequestParam String token,
            @Parameter(description = "true = aprovado | false = recusado") @RequestParam boolean approved) {

        ApproveQuotationCommand command      = new ApproveQuotationCommand(id, token, approved);
        ServiceOrder            serviceOrder = approveQuotationUseCase.execute(command);
        return ResponseEntity.ok(mapper.toResponse(serviceOrder));
    }
}
