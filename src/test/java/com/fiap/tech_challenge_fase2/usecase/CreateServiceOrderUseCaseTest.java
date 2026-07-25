package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.dto.CreateServiceOrderCommand;
import com.fiap.tech_challenge_fase2.application.port.out.EmailNotificationGateway;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.CreateServiceOrderUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateServiceOrderUseCase")
class CreateServiceOrderUseCaseTest {

    @Mock private ServiceOrderRepositoryPort repository;
    @Mock private EmailNotificationGateway   emailGateway;

    private CreateServiceOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateServiceOrderUseCaseImpl(repository, emailGateway);
    }

    @Test
    @DisplayName("Deve criar OS com status RECEIVED e enviar e-mail")
    void shouldCreateAndSendEmail() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder result = useCase.execute(buildCommand("Maria Lima", "maria@test.com"));

        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(result.getCustomer().getName()).isEqualTo("Maria Lima");
        assertThat(result.getOrderNumber()).startsWith("OS-");

        verify(repository, times(1)).save(any());
        verify(emailGateway, times(1)).sendStatusUpdateEmail(any());
        verify(emailGateway, never()).sendQuotationApprovalEmail(any());
    }

    @Test
    @DisplayName("Deve criar OS com serviços e peças")
    void shouldCreateWithServicesAndParts() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateServiceOrderCommand command = new CreateServiceOrderCommand(
                new CreateServiceOrderCommand.CustomerData("Carlos", "carlos@test.com", null),
                new CreateServiceOrderCommand.VehicleData("XYZ-0000", "VW", "Gol", 2020, "Branco"),
                List.of(new CreateServiceOrderCommand.ServiceItemData("Revisão", null, new BigDecimal("200.00"), 2.0)),
                List.of(new CreateServiceOrderCommand.PartItemData("Filtro", "F-001", 1, new BigDecimal("50.00"))),
                "Revisão completa"
        );

        ServiceOrder result = useCase.execute(command);

        assertThat(result.getServices()).hasSize(1);
        assertThat(result.getParts()).hasSize(1);
        assertThat(result.calculateTotalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    private CreateServiceOrderCommand buildCommand(String name, String email) {
        return new CreateServiceOrderCommand(
                new CreateServiceOrderCommand.CustomerData(name, email, "11999999999"),
                new CreateServiceOrderCommand.VehicleData("ABC-1234", "Toyota", "Corolla", 2022, "Prata"),
                List.of(),
                List.of(),
                null
        );
    }
}
