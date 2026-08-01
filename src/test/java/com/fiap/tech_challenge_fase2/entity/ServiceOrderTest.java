package com.fiap.tech_challenge_fase2.entity;

import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ServiceOrder — Regras de Negócio do Domínio")
class ServiceOrderTest {

    private Customer customer;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        customer = Customer.create("João Silva", "joao@email.com", "11999999999");
        vehicle  = Vehicle.create("ABC-1234", "Toyota", "Corolla", 2022, "Prata");
    }

    @Test
    @DisplayName("Deve abrir OS com status RECEIVED e número de OS gerado")
    void shouldOpenServiceOrderWithReceivedStatus() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), "Obs");

        assertThat(os.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(os.getOrderNumber()).startsWith("OS-");
        assertThat(os.getId()).isNotBlank();
        assertThat(os.isDeleted()).isFalse();
        assertThat(os.getApprovalToken()).isNull();
        assertThat(os.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir transição válida RECEIVED → DIAGNOSIS")
    void shouldAllowReceivedToDiagnosis() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);

        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);

        assertThat(os.getStatus()).isEqualTo(ServiceOrderStatus.DIAGNOSIS);
    }

    @Test
    @DisplayName("Deve lançar exceção para transição inválida RECEIVED → EXECUTION")
    void shouldThrowOnInvalidTransitionReceivedToExecution() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);

        assertThatThrownBy(() -> os.transitionTo(ServiceOrderStatus.EXECUTION))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("RECEIVED")
                .hasMessageContaining("EXECUTION");
    }

    @Test
    @DisplayName("Deve percorrer o fluxo completo até DELIVERED")
    void shouldFollowCompleteHappyPath() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);

        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        os.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
        os.generateApprovalToken();
        os.approve(os.getApprovalToken());
        os.transitionTo(ServiceOrderStatus.FINISHED);
        os.transitionTo(ServiceOrderStatus.DELIVERED);

        assertThat(os.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Deve gerar token de aprovação ao chamar generateApprovalToken()")
    void shouldGenerateApprovalToken() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        os.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
        os.generateApprovalToken();

        assertThat(os.getApprovalToken()).isNotBlank();
    }

    @Test
    @DisplayName("Deve aprovar orçamento e avançar para EXECUTION")
    void shouldApproveAndMoveToExecution() {
        ServiceOrder os = prepareAwaitingApproval();
        String token = os.getApprovalToken();

        os.approve(token);

        assertThat(os.getStatus()).isEqualTo(ServiceOrderStatus.EXECUTION);
        assertThat(os.getApprovalToken()).isNull();
    }

    @Test
    @DisplayName("Deve recusar orçamento e retornar para DIAGNOSIS")
    void shouldRefuseAndReturnToDiagnosis() {
        ServiceOrder os = prepareAwaitingApproval();
        String token = os.getApprovalToken();

        os.refuse(token);

        assertThat(os.getStatus()).isEqualTo(ServiceOrderStatus.DIAGNOSIS);
        assertThat(os.getApprovalToken()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao aprovar com token inválido")
    void shouldThrowOnInvalidToken() {
        ServiceOrder os = prepareAwaitingApproval();

        assertThatThrownBy(() -> os.approve("token-errado"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Token");
    }

    @Test
    @DisplayName("Deve lançar exceção ao aprovar OS que não está em AWAITING_APPROVAL")
    void shouldThrowWhenNotAwaitingApproval() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        os.generateApprovalToken();
        String token = os.getApprovalToken();

        assertThatThrownBy(() -> os.approve(token))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }


    @Test
    @DisplayName("Deve calcular total corretamente: serviços + peças")
    void shouldCalculateTotalAmount() {
        ServiceItem troca = ServiceItem.create("Troca de Óleo", null, new BigDecimal("150.00"), 1.0);
        PartItem filtro = PartItem.create("Filtro de Óleo", "FO-123", 2, new BigDecimal("35.00"));

        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(troca), List.of(filtro), null);

        assertThat(os.calculateTotalAmount()).isEqualByComparingTo(new BigDecimal("220.00"));
    }

    @Test
    @DisplayName("Deve retornar zero quando não há serviços nem peças")
    void shouldReturnZeroTotalWhenEmpty() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);

        assertThat(os.calculateTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    @DisplayName("Deve marcar OS como deletada (soft delete)")
    void shouldSoftDelete() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
        os.softDelete();

        assertThat(os.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar serviços e peças durante o diagnóstico")
    void shouldUpdateItemsSuccessfully() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), "Troca simples");
        ServiceItem s1 = ServiceItem.create("Alinhamento", "Alinhamento 3D", new BigDecimal("100.00"), 1.0);
        PartItem p1 = PartItem.create("Pastilha de Freio", "PF-100", 2, new BigDecimal("80.00"));

        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        os.updateItems(List.of(s1), List.of(p1));

        assertThat(os.getServices()).hasSize(1);
        assertThat(os.getParts()).hasSize(1);
        assertThat(os.calculateTotalAmount()).isEqualByComparingTo(new BigDecimal("260.00"));
    }


    private ServiceOrder prepareAwaitingApproval() {
        ServiceOrder os = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
        os.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        os.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
        os.generateApprovalToken();
        return os;
    }
}
