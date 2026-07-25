package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.dto.ApproveQuotationCommand;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.ApproveQuotationUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveQuotationUseCase")
class ApproveQuotationUseCaseTest {

    @Mock private ServiceOrderRepositoryPort repository;

    private ApproveQuotationUseCaseImpl useCase;
    private ServiceOrder                 orderAwaitingApproval;
    private String                       validToken;

    @BeforeEach
    void setUp() {
        useCase = new ApproveQuotationUseCaseImpl(repository);

        Customer customer = Customer.create("Pedro Alves", "pedro@test.com", "11977776666");
        Vehicle  vehicle  = Vehicle.create("PDR-9999", "Honda", "Fit", 2021, "Azul");

        orderAwaitingApproval = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
        orderAwaitingApproval.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        orderAwaitingApproval.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
        orderAwaitingApproval.generateApprovalToken();
        validToken = orderAwaitingApproval.getApprovalToken();
    }

    @Test
    @DisplayName("Deve aprovar orçamento e mover para EXECUTION")
    void shouldApproveAndMoveToExecution() {
        when(repository.findByApprovalToken(validToken))
                .thenReturn(Optional.of(orderAwaitingApproval));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = useCase.execute(
                new ApproveQuotationCommand(orderAwaitingApproval.getId(), validToken, true));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.EXECUTION);
    }

    @Test
    @DisplayName("Deve recusar orçamento e retornar para DIAGNOSIS")
    void shouldRefuseAndReturnToDiagnosis() {
        when(repository.findByApprovalToken(validToken))
                .thenReturn(Optional.of(orderAwaitingApproval));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = useCase.execute(
                new ApproveQuotationCommand(orderAwaitingApproval.getId(), validToken, false));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.DIAGNOSIS);
    }

    @Test
    @DisplayName("Deve lançar exceção quando token é inválido")
    void shouldThrowWhenTokenNotFound() {
        when(repository.findByApprovalToken("token-invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new ApproveQuotationCommand("any-id", "token-invalido", true)))
                .isInstanceOf(ServiceOrderNotFoundException.class);
    }
}
