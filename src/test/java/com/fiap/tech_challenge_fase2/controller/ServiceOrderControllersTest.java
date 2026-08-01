package com.fiap.tech_challenge_fase2.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.tech_challenge_fase2.application.port.in.*;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;
import com.fiap.tech_challenge_fase2.interfaces.controller.GlobalExceptionHandler;
import com.fiap.tech_challenge_fase2.interfaces.controller.QuotationApprovalController;
import com.fiap.tech_challenge_fase2.interfaces.controller.ServiceOrderOpeningController;
import com.fiap.tech_challenge_fase2.interfaces.controller.ServiceOrderStatusController;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.CreateServiceOrderRequest;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.UpdateStatusRequest;
import com.fiap.tech_challenge_fase2.interfaces.mapper.ServiceOrderMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ServiceOrderOpeningController.class,
        ServiceOrderStatusController.class,
        QuotationApprovalController.class,
        GlobalExceptionHandler.class
})
@Import(ServiceOrderMapper.class)
@DisplayName("Controllers — Testes de Integração")
class ServiceOrderControllersTest {

    @Autowired private MockMvc     mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CreateServiceOrderUseCase         createUseCase;
    @MockBean private GetServiceOrderStatusUseCase      getStatusUseCase;
    @MockBean private ListServiceOrdersUseCase          listUseCase;
    @MockBean private UpdateServiceOrderStatusUseCase   updateStatusUseCase;
    @MockBean private ApproveQuotationUseCase           approveUseCase;

    // ── ServiceOrderOpeningController ────────────────────────────────────────

    @Test
    @DisplayName("POST /api/service-orders — deve retornar 201 com OS criada")
    void shouldReturn201WhenCreatingServiceOrder() throws Exception {
        ServiceOrder mock = buildMockOrder();
        when(createUseCase.execute(any())).thenReturn(mock);

        mockMvc.perform(post("/api/service-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.customer.name").value("Lucas Pereira"))
                .andExpect(jsonPath("$.orderNumber").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/service-orders — deve retornar 400 quando request inválida")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        String badJson = """
                { "customer": { "name": "", "email": "nao-e-email" }, "vehicle": null }
                """;

        mockMvc.perform(post("/api/service-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"));
    }

    // ── ServiceOrderStatusController ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/service-orders/{id}/status — deve retornar status da OS")
    void shouldReturnServiceOrderStatus() throws Exception {
        ServiceOrder mock = buildMockOrder();
        when(getStatusUseCase.execute(mock.getId())).thenReturn(mock);

        mockMvc.perform(get("/api/service-orders/{id}/status", mock.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.statusDescription").value("Recebida"));
    }

    @Test
    @DisplayName("GET /api/service-orders/{id}/status — deve retornar 404 quando não encontrada")
    void shouldReturn404WhenNotFound() throws Exception {
        when(getStatusUseCase.execute("id-inexistente"))
                .thenThrow(new ServiceOrderNotFoundException("id-inexistente"));

        mockMvc.perform(get("/api/service-orders/{id}/status", "id-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("OS não encontrada"));
    }

    @Test
    @DisplayName("GET /api/service-orders — deve retornar lista de OS")
    void shouldReturnListOfOrders() throws Exception {
        when(listUseCase.execute()).thenReturn(List.of(buildMockOrder()));

        mockMvc.perform(get("/api/service-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    @Test
    @DisplayName("PATCH /api/service-orders/{id}/status — deve atualizar status")
    void shouldUpdateStatus() throws Exception {
        ServiceOrder mock = buildMockOrderWithStatus(ServiceOrderStatus.DIAGNOSIS);
        when(updateStatusUseCase.execute(any())).thenReturn(mock);

        mockMvc.perform(patch("/api/service-orders/{id}/status", mock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateStatusRequest(ServiceOrderStatus.DIAGNOSIS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIAGNOSIS"));
    }

    @Test
    @DisplayName("PATCH /api/service-orders/{id}/status — deve atualizar status enviando serviços e peças no diagnóstico")
    void shouldUpdateStatusWithServicesAndParts() throws Exception {
        ServiceOrder mock = buildMockOrderWithStatus(ServiceOrderStatus.DIAGNOSIS);
        when(updateStatusUseCase.execute(any())).thenReturn(mock);

        CreateServiceOrderRequest.ServiceItemRequest sReq = new CreateServiceOrderRequest.ServiceItemRequest(
                "Troca de Óleo", "Desc", new BigDecimal("150.00"), 1.0);
        CreateServiceOrderRequest.PartItemRequest pReq = new CreateServiceOrderRequest.PartItemRequest(
                "Filtro", "P-100", 1, new BigDecimal("40.00"));

        UpdateStatusRequest req = new UpdateStatusRequest(
                ServiceOrderStatus.DIAGNOSIS, List.of(sReq), List.of(pReq));

        mockMvc.perform(patch("/api/service-orders/{id}/status", mock.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIAGNOSIS"));
    }

    @Test
    @DisplayName("GET /api/quotations/{id}?token=X&approved=true — deve aprovar orçamento")
    void shouldApproveQuotation() throws Exception {
        ServiceOrder mock = buildMockOrderWithStatus(ServiceOrderStatus.EXECUTION);
        when(approveUseCase.execute(any())).thenReturn(mock);

        mockMvc.perform(get("/api/quotations/{id}", mock.getId())
                        .param("token", "valid-token")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTION"));
    }

    @Test
    @DisplayName("GET /api/quotations/{id}?token=X&approved=false — deve recusar orçamento")
    void shouldRefuseQuotation() throws Exception {
        ServiceOrder mock = buildMockOrderWithStatus(ServiceOrderStatus.DIAGNOSIS);
        when(approveUseCase.execute(any())).thenReturn(mock);

        mockMvc.perform(get("/api/quotations/{id}", mock.getId())
                        .param("token", "valid-token")
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIAGNOSIS"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ServiceOrder buildMockOrder() {
        Customer customer = Customer.create("Lucas Pereira", "lucas@test.com", "11955554444");
        Vehicle  vehicle  = Vehicle.create("LUC-0001", "Renault", "Kwid", 2023, "Cinza");
        return ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
    }

    private ServiceOrder buildMockOrderWithStatus(ServiceOrderStatus status) {
        Customer     customer = Customer.create("Lucas Pereira", "lucas@test.com", "11955554444");
        Vehicle      vehicle  = Vehicle.create("LUC-0001", "Renault", "Kwid", 2023, "Cinza");
        ServiceOrder os       = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);

        switch (status) {
            case DIAGNOSIS -> os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
            case AWAITING_APPROVAL -> {
                os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
                os.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
                os.generateApprovalToken();
            }
            case EXECUTION -> {
                os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
                os.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
                os.generateApprovalToken();
                os.approve(os.getApprovalToken());
            }
            default -> { /* RECEIVED — já é o padrão */ }
        }
        return os;
    }

    private CreateServiceOrderRequest buildCreateRequest() {
        return new CreateServiceOrderRequest(
                new CreateServiceOrderRequest.CustomerRequest("Lucas Pereira", "lucas@test.com", "11955554444"),
                new CreateServiceOrderRequest.VehicleRequest("LUC-0001", "Renault", "Kwid", 2023, "Cinza"),
                List.of(new CreateServiceOrderRequest.ServiceItemRequest(
                        "Revisão geral", null, new BigDecimal("300.00"), 3.0)),
                List.of(),
                null
        );
    }
}
