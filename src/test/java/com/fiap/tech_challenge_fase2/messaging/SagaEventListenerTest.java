package com.fiap.tech_challenge_fase2.messaging;

import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.Customer;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.entity.Vehicle;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.SagaEventListener;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaEventListenerTest {

    @Mock
    private ServiceOrderRepositoryPort repository;

    @InjectMocks
    private SagaEventListener sagaEventListener;

    private ServiceOrder serviceOrder;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.create("João Silva", "joao@email.com", "11999999999");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Toyota", "Corolla", 2022, "Prata");
        serviceOrder = ServiceOrder.open(customer, vehicle, List.of(), List.of(), "Obs");
    }

    @Test
    @DisplayName("Deve transicionar OS para DELIVERED quando pagamento for aprovado e status for FINISHED")
    void shouldHandlePaymentApprovedWhenFinished() {
        serviceOrder.transitionTo(ServiceOrderStatus.DIAGNOSIS);
        serviceOrder.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
        serviceOrder.generateApprovalToken();
        serviceOrder.approve(serviceOrder.getApprovalToken());
        serviceOrder.transitionTo(ServiceOrderStatus.FINISHED);

        when(repository.findById(serviceOrder.getId())).thenReturn(Optional.of(serviceOrder));

        ServiceOrderEvents.PaymentApprovedEvent event = new ServiceOrderEvents.PaymentApprovedEvent(
                serviceOrder.getId(), "PAY-123");

        sagaEventListener.handlePaymentApproved(event);

        assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        verify(repository, times(1)).save(serviceOrder);
    }

    @Test
    @DisplayName("Deve transicionar OS para CANCELED no evento PaymentFailed (Rollback)")
    void shouldHandlePaymentFailed() {
        serviceOrder.transitionTo(ServiceOrderStatus.DIAGNOSIS);

        when(repository.findById(serviceOrder.getId())).thenReturn(Optional.of(serviceOrder));

        ServiceOrderEvents.PaymentFailedEvent event = new ServiceOrderEvents.PaymentFailedEvent(
                serviceOrder.getId(), "PIX expirado");

        sagaEventListener.handlePaymentFailed(event);

        assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELED);
        verify(repository, times(1)).save(serviceOrder);
    }
}
