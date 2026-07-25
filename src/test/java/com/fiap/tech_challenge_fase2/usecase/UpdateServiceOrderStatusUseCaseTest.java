package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.UpdateServiceOrderStatusUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateServiceOrderStatusUseCase")
class UpdateServiceOrderStatusUseCaseTest {

    @Mock private ServiceOrderRepositoryPort repository;
    @Mock private EventPublisher eventPublisher;

    private UpdateServiceOrderStatusUseCaseImpl useCase;
    private ServiceOrder                         baseOrder;

    @BeforeEach
    void setUp() {
        useCase = new UpdateServiceOrderStatusUseCaseImpl(repository, eventPublisher);
        Customer customer = Customer.create("Ana Costa", "ana@test.com", "11988887777");
        Vehicle  vehicle  = Vehicle.create("ANA-0001", "Fiat", "Uno", 2019, "Vermelho");
        baseOrder = ServiceOrder.open(customer, vehicle, List.of(), List.of(), null);
    }

    @Test
    @DisplayName("Deve atualizar RECEIVED → DIAGNOSIS")
    void shouldUpdateReceivedToDiagnosis() {
        when(repository.findById(baseOrder.getId())).thenReturn(Optional.of(baseOrder));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = useCase.execute(
                new UpdateStatusCommand(baseOrder.getId(), ServiceOrderStatus.DIAGNOSIS));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.DIAGNOSIS);
    }

    @Test
    @DisplayName("Deve gerar token e publicar evento de orçamentamento ao atingir AWAITING_APPROVAL")
    void shouldSendApprovalEmailWhenAwaitingApproval() {
        baseOrder.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        when(repository.findById(baseOrder.getId())).thenReturn(Optional.of(baseOrder));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = useCase.execute(
                new UpdateStatusCommand(baseOrder.getId(), ServiceOrderStatus.AWAITING_APPROVAL));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        assertThat(result.getApprovalToken()).isNotBlank();
        verify(eventPublisher).publishEvent(anyString(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando OS não é encontrada")
    void shouldThrowWhenNotFound() {
        when(repository.findById("id-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateStatusCommand("id-inexistente", ServiceOrderStatus.DIAGNOSIS)))
                .isInstanceOf(ServiceOrderNotFoundException.class);
    }
}
