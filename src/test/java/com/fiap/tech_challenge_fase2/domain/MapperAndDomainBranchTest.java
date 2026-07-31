package com.fiap.tech_challenge_fase2.domain;

import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.InvalidStatusTransitionException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.SagaEventListener;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.interfaces.controller.GlobalExceptionHandler;
import com.fiap.tech_challenge_fase2.interfaces.dto.request.CreateServiceOrderRequest;
import com.fiap.tech_challenge_fase2.interfaces.dto.response.ServiceOrderResponse;
import com.fiap.tech_challenge_fase2.interfaces.mapper.ServiceOrderMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ProblemDetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapperAndDomainBranchTest {

    @Test
    @DisplayName("ServiceOrderMapper — Deve cobrir listas nulas e não nulas")
    void testServiceOrderMapperNullAndNonNullBranches() {
        ServiceOrderMapper mapper = new ServiceOrderMapper();

        CreateServiceOrderRequest.CustomerRequest custReq = new CreateServiceOrderRequest.CustomerRequest("João", "j@e.com", "1199");
        CreateServiceOrderRequest.VehicleRequest vehReq = new CreateServiceOrderRequest.VehicleRequest("ABC-1234", "Fiat", "Uno", 2010, "Branco");

        CreateServiceOrderRequest requestNullItems = new CreateServiceOrderRequest(custReq, vehReq, null, null, "Obs");
        CreateServiceOrderCommand cmdNull = mapper.toCommand(requestNullItems);

        assertThat(cmdNull.services()).isEmpty();
        assertThat(cmdNull.parts()).isEmpty();

        CreateServiceOrderRequest.ServiceItemRequest sReq = new CreateServiceOrderRequest.ServiceItemRequest("Troca", "Óleo", new BigDecimal("100"), 1.0);
        CreateServiceOrderRequest.PartItemRequest pReq = new CreateServiceOrderRequest.PartItemRequest("Filtro", "F1", 1, new BigDecimal("50"));
        CreateServiceOrderRequest requestWithItems = new CreateServiceOrderRequest(custReq, vehReq, List.of(sReq), List.of(pReq), "Obs");

        CreateServiceOrderCommand cmdWithItems = mapper.toCommand(requestWithItems);
        assertThat(cmdWithItems.services()).hasSize(1);
        assertThat(cmdWithItems.parts()).hasSize(1);

        Customer customer = Customer.create("João", "j@e.com", "1199");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Fiat", "Uno", 2010, "Branco");
        ServiceItem serviceItem = ServiceItem.create("Troca", "Óleo", new BigDecimal("100"), 1.0);
        PartItem partItem = PartItem.create("Filtro", "F1", 1, new BigDecimal("50"));
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(serviceItem), List.of(partItem), "Obs");

        ServiceOrderResponse response = mapper.toResponse(os);
        assertThat(response.id()).isEqualTo(os.getId());

        List<ServiceOrderResponse> responses = mapper.toResponseList(List.of(os));
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("ServiceOrder — Deve cobrir ramificações do construtor, recusa e exceções de token")
    void testServiceOrderConstructorBranches() {
        Customer customer = Customer.create("João", "j@e.com", "1199");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Fiat", "Uno", 2010, "Branco");

        // Construtor completo com valores explícitos (não nulos e nulos para id/createdAt/updatedAt/services/parts)
        LocalDateTime now = LocalDateTime.now();
        ServiceOrder osExplicit = new ServiceOrder(
                "os-1", "OS-99", ServiceOrderStatus.AWAITING_APPROVAL,
                customer, vehicle, null, null, "token-xyz", "Obs", false, now, now
        );

        assertThat(osExplicit.getId()).isEqualTo("os-1");
        assertThat(osExplicit.getServices()).isEmpty();
        assertThat(osExplicit.getParts()).isEmpty();

        // Refusão de orçamento com token válido
        osExplicit.refuse("token-xyz");
        assertThat(osExplicit.getStatus()).isEqualTo(ServiceOrderStatus.DIAGNOSIS);
        assertThat(osExplicit.getApprovalToken()).isNull();

        // Refusão com token nulo
        assertThatThrownBy(() -> osExplicit.refuse(null))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Token");
    }

    @Test
    @DisplayName("SagaEventListener — Deve cobrir ramificações onde OS não é FINISHED/AWAITING_APPROVAL")
    void testSagaEventListenerIgnoredStatuses() {
        ServiceOrderRepositoryPort repo = Mockito.mock(ServiceOrderRepositoryPort.class);
        SagaEventListener listener = new SagaEventListener(repo);

        Customer customer = Customer.create("João", "j@e.com", "1199");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Fiat", "Uno", 2010, "Branco");
        ServiceOrder osReceived = ServiceOrder.open(customer, vehicle, List.of(), List.of(), "Obs");

        Mockito.when(repo.findById(osReceived.getId())).thenReturn(Optional.of(osReceived));

        // Pagamento aprovado em OS no status RECEIVED (deve ignorar e não salvar)
        listener.handlePaymentApproved(new ServiceOrderEvents.PaymentApprovedEvent(osReceived.getId(), "P123"));

        assertThat(osReceived.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        Mockito.verify(repo, Mockito.never()).save(osReceived);
    }

    @Test
    @DisplayName("GlobalExceptionHandler — Deve tratar exceções genéricas")
    void testGlobalExceptionHandlerGenericException() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getMethod()).thenReturn("GET");
        Mockito.when(request.getRequestURI()).thenReturn("/api/test");

        ProblemDetail pd = handler.handleGeneric(new RuntimeException("Erro inesperado"), request);
        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.getDetail()).isEqualTo("Erro interno inesperado");
    }
}
