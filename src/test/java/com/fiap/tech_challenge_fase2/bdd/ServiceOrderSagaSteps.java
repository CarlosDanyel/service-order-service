package com.fiap.tech_challenge_fase2.bdd;

import com.fiap.tech_challenge_fase2.application.dto.ApproveQuotationCommand;
import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.application.dto.UpdateStatusCommand;
import com.fiap.tech_challenge_fase2.application.usecase.ApproveQuotationUseCaseImpl;
import com.fiap.tech_challenge_fase2.application.usecase.CreateServiceOrderUseCaseImpl;
import com.fiap.tech_challenge_fase2.application.usecase.UpdateServiceOrderStatusUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.SagaEventListener;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.ServiceOrderEvents;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter.ServiceOrderPersistenceAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.ServiceOrderJpaRepository;
import io.cucumber.java.pt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ServiceOrderSagaSteps {

    @Autowired private ServiceOrderJpaRepository repository;

    private CreateServiceOrderUseCaseImpl createUseCase;
    private UpdateServiceOrderStatusUseCaseImpl updateStatusUseCase;
    private ApproveQuotationUseCaseImpl approveUseCase;
    private SagaEventListener sagaListener;

    private CreateServiceOrderCommand createCommand;
    private ServiceOrder currentOrder;

    @Dado("que o cliente {string} abre uma OS para o veículo {string} marca {string} modelo {string}")
    public void setupCustomerAndVehicle(String name, String plate, String brand, String model) {
        EventPublisher dummyPublisher = org.mockito.Mockito.mock(EventPublisher.class);
        ServiceOrderPersistenceAdapter persistenceAdapter = new ServiceOrderPersistenceAdapter(repository);

        createUseCase = new CreateServiceOrderUseCaseImpl(persistenceAdapter, dummyPublisher);
        updateStatusUseCase = new UpdateServiceOrderStatusUseCaseImpl(persistenceAdapter, dummyPublisher);
        approveUseCase = new ApproveQuotationUseCaseImpl(persistenceAdapter);
        sagaListener = new SagaEventListener(persistenceAdapter);

        createCommand = new CreateServiceOrderCommand(
                new CreateServiceOrderCommand.CustomerData(name, "carlos@test.com", "11999998888"),
                new CreateServiceOrderCommand.VehicleData(plate, brand, model, 2022, "Preto"),
                List.of(new CreateServiceOrderCommand.ServiceItemData("Troca de óleo", "Troca completa", new BigDecimal("150.00"), 1.0)),
                List.of(),
                "Manutenção preventiva"
        );
    }

    @Quando("a OS é registrada no sistema")
    public void registerServiceOrder() {
        currentOrder = createUseCase.execute(createCommand);
    }

    @Então("a OS deve ser criada com o status {string}")
    public void verifyStatusCreated(String expectedStatus) {
        assertThat(currentOrder.getStatus().name()).isEqualTo(expectedStatus);
    }

    @E("a OS avança para o status {string}")
    public void updateStatusDiagnosis(String nextStatus) {
        currentOrder = updateStatusUseCase.execute(new UpdateStatusCommand(currentOrder.getId(), ServiceOrderStatus.valueOf(nextStatus)));
        assertThat(currentOrder.getStatus().name()).isEqualTo(nextStatus);
    }

    @E("o orçamento é gerado mudando o status para {string}")
    public void generateQuotation(String nextStatus) {
        currentOrder = updateStatusUseCase.execute(new UpdateStatusCommand(currentOrder.getId(), ServiceOrderStatus.valueOf(nextStatus)));
        assertThat(currentOrder.getStatus().name()).isEqualTo(nextStatus);
        assertThat(currentOrder.getApprovalToken()).isNotNull();
    }

    @E("o cliente aprova o orçamento mudando o status para {string}")
    public void approveQuotation(String expectedStatus) {
        currentOrder = approveUseCase.execute(new ApproveQuotationCommand(currentOrder.getId(), currentOrder.getApprovalToken(), true));
        assertThat(currentOrder.getStatus().name()).isEqualTo(expectedStatus);
    }

    @E("a execução é concluída mudando o status para {string}")
    public void finishExecution(String expectedStatus) {
        currentOrder = updateStatusUseCase.execute(new UpdateStatusCommand(currentOrder.getId(), ServiceOrderStatus.valueOf(expectedStatus)));
        assertThat(currentOrder.getStatus().name()).isEqualTo(expectedStatus);
    }

    @E("o pagamento é aprovado no serviço de billing finalizando o status da OS como {string}")
    public void simulatePaymentApproved(String expectedStatus) {
        sagaListener.handlePaymentApproved(new ServiceOrderEvents.PaymentApprovedEvent(currentOrder.getId(), "EXT-PAY-123"));
        ServiceOrderPersistenceAdapter persistenceAdapter = new ServiceOrderPersistenceAdapter(repository);
        currentOrder = persistenceAdapter.findById(currentOrder.getId()).orElseThrow();
        assertThat(currentOrder.getStatus().name()).isEqualTo(expectedStatus);
    }

    @Dado("que uma OS com valor de orçamento aprovado está aguardando confirmação financeira")
    public void setupOrderAwaitingPayment() {
        setupCustomerAndVehicle("João Silva", "XYZ-9999", "Fiat", "Uno");
        registerServiceOrder();
        updateStatusDiagnosis("DIAGNOSIS");
        generateQuotation("AWAITING_APPROVAL");
    }

    @Quando("o serviço de pagamento rejeita o pagamento da cobrança")
    public void simulatePaymentRejected() {
        sagaListener.handlePaymentFailed(new ServiceOrderEvents.PaymentFailedEvent(currentOrder.getId(), "Cartão Recusado"));
    }

    @Então("o mecanismo de Saga dispara a compensação alterando o status da OS para {string}")
    public void verifySagaRollback(String expectedStatus) {
        ServiceOrderPersistenceAdapter persistenceAdapter = new ServiceOrderPersistenceAdapter(repository);
        currentOrder = persistenceAdapter.findById(currentOrder.getId()).orElseThrow();
        assertThat(currentOrder.getStatus().name()).isEqualTo(expectedStatus);
    }
}
