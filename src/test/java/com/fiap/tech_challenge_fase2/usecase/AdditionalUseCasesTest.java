package com.fiap.tech_challenge_fase2.usecase;

import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.GetServiceOrderStatusUseCaseImpl;
import com.fiap.tech_challenge_fase2.application.usecase.ListServiceOrdersUseCaseImpl;
import com.fiap.tech_challenge_fase2.domain.entity.Customer;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.entity.Vehicle;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdditionalUseCasesTest {

    @Mock
    private ServiceOrderRepositoryPort repository;

    private GetServiceOrderStatusUseCaseImpl getStatusUseCase;
    private ListServiceOrdersUseCaseImpl listUseCase;

    private ServiceOrder serviceOrder;

    @BeforeEach
    void setUp() {
        getStatusUseCase = new GetServiceOrderStatusUseCaseImpl(repository);
        listUseCase = new ListServiceOrdersUseCaseImpl(repository);

        Customer customer = Customer.create("João Silva", "joao@email.com", "11999999999");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Toyota", "Corolla", 2022, "Prata");
        serviceOrder = ServiceOrder.open(customer, vehicle, List.of(), List.of(), "Obs");
    }

    @Test
    @DisplayName("GetServiceOrderStatusUseCase — Deve retornar a OS por ID")
    void getStatus_ShouldReturnServiceOrder() {
        when(repository.findById(serviceOrder.getId())).thenReturn(Optional.of(serviceOrder));

        ServiceOrder result = getStatusUseCase.execute(serviceOrder.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(serviceOrder.getId());
    }

    @Test
    @DisplayName("GetServiceOrderStatusUseCase — Deve lançar exceção se OS não for encontrada")
    void getStatus_ShouldThrowExceptionWhenNotFound() {
        when(repository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getStatusUseCase.execute("invalid-id"))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessageContaining("invalid-id");
    }

    @Test
    @DisplayName("ListServiceOrdersUseCase — Deve listar OS ativas")
    void listServiceOrders_ShouldReturnActiveOrders() {
        when(repository.findActiveOrdered()).thenReturn(List.of(serviceOrder));

        List<ServiceOrder> result = listUseCase.execute();

        assertThat(result).hasSize(1);
    }
}
