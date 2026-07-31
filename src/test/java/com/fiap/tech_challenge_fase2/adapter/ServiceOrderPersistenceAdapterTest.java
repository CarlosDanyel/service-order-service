package com.fiap.tech_challenge_fase2.adapter;

import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter.ServiceOrderPersistenceAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.CustomerJpaEntity;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.ServiceOrderJpaEntity;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.VehicleJpaEntity;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderPersistenceAdapterTest {

    @Mock
    private ServiceOrderJpaRepository jpaRepository;

    @InjectMocks
    private ServiceOrderPersistenceAdapter adapter;

    private ServiceOrder domainOrder;
    private ServiceOrderJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.create("João Silva", "joao@email.com", "11999999999");
        Vehicle vehicle = Vehicle.create("ABC-1234", "Toyota", "Corolla", 2022, "Prata");
        ServiceItem service = ServiceItem.create("Troca de óleo", "Troca completa", new BigDecimal("150.00"), 1.0);
        PartItem part = PartItem.create("Filtro", "P-100", 1, new BigDecimal("50.00"));

        domainOrder = ServiceOrder.open(customer, vehicle, List.of(service), List.of(part), "Obs");

        CustomerJpaEntity customerJpa = CustomerJpaEntity.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();

        VehicleJpaEntity vehicleJpa = VehicleJpaEntity.builder()
                .id(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .build();

        jpaEntity = ServiceOrderJpaEntity.builder()
                .id(domainOrder.getId())
                .orderNumber(domainOrder.getOrderNumber())
                .status(domainOrder.getStatus())
                .customer(customerJpa)
                .vehicle(vehicleJpa)
                .services(new ArrayList<>())
                .parts(new ArrayList<>())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve salvar ServiceOrder convertendo para JPA e de volta para Domain")
    void shouldSaveServiceOrder() {
        when(jpaRepository.save(any(ServiceOrderJpaEntity.class))).thenReturn(jpaEntity);

        ServiceOrder saved = adapter.save(domainOrder);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(domainOrder.getId());
        verify(jpaRepository, times(1)).save(any(ServiceOrderJpaEntity.class));
    }

    @Test
    @DisplayName("Deve buscar ServiceOrder por ID")
    void shouldFindById() {
        when(jpaRepository.findById(domainOrder.getId())).thenReturn(Optional.of(jpaEntity));

        Optional<ServiceOrder> result = adapter.findById(domainOrder.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(domainOrder.getId());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar ID inexistente")
    void shouldReturnEmptyWhenNotFoundById() {
        when(jpaRepository.findById("invalid-id")).thenReturn(Optional.empty());

        Optional<ServiceOrder> result = adapter.findById("invalid-id");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar ServiceOrder por Token de Aprovação")
    void shouldFindByApprovalToken() {
        when(jpaRepository.findByApprovalToken("token-123")).thenReturn(Optional.of(jpaEntity));

        Optional<ServiceOrder> result = adapter.findByApprovalToken("token-123");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Deve buscar Ordens de Serviço ativas ordenadas")
    void shouldFindActiveOrdered() {
        when(jpaRepository.findActiveOrdered(anyList())).thenReturn(List.of(jpaEntity));

        List<ServiceOrder> result = adapter.findActiveOrdered();

        assertThat(result).hasSize(1);
    }
}
