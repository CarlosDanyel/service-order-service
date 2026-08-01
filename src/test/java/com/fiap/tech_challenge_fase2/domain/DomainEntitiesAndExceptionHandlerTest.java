package com.fiap.tech_challenge_fase2.domain;

import com.fiap.tech_challenge_fase2.domain.entity.*;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.domain.exception.InvalidStatusTransitionException;
import com.fiap.tech_challenge_fase2.domain.exception.ResourceNotFoundException;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;
import com.fiap.tech_challenge_fase2.interfaces.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntitiesAndExceptionHandlerTest {

    @Test
    @DisplayName("Deve testar getters, construtores, equals e hashCode de Customer")
    void testCustomerEqualsAndHashCode() {
        Customer c1 = new Customer(null, "Maria", "maria@email.com", "11988887777");
        Customer c2 = new Customer("id1", "Maria", "maria@email.com", "11988887777");
        Customer c3 = new Customer("id1", "Maria", "maria@email.com", "11988887777");
        Customer c4 = new Customer("id2", "Maria", "maria@email.com", "11988887777");

        assertThat(c1.getId()).isNotBlank();
        assertThat(c1.getName()).isEqualTo("Maria");
        assertThat(c1.getEmail()).isEqualTo("maria@email.com");
        assertThat(c1.getPhone()).isEqualTo("11988887777");

        assertThat(c2).isEqualTo(c2);
        assertThat(c2).isEqualTo(c3);
        assertThat(c2).isNotEqualTo(c4);
        assertThat(c2).isNotEqualTo(null);
        assertThat(c2).isNotEqualTo("String");
        assertThat(c2.hashCode()).isEqualTo(c3.hashCode());
    }

    @Test
    @DisplayName("Deve testar getters, construtores, equals e hashCode de Vehicle")
    void testVehicleEqualsAndHashCode() {
        Vehicle v1 = new Vehicle(null, "XYZ-9999", "Honda", "Civic", 2023, "Preto");
        Vehicle v2 = new Vehicle("v1", "XYZ-9999", "Honda", "Civic", 2023, "Preto");
        Vehicle v3 = new Vehicle("v1", "XYZ-9999", "Honda", "Civic", 2023, "Preto");
        Vehicle v4 = new Vehicle("v2", "XYZ-9999", "Honda", "Civic", 2023, "Preto");

        assertThat(v1.getId()).isNotBlank();
        assertThat(v1.getLicensePlate()).isEqualTo("XYZ-9999");
        assertThat(v1.getBrand()).isEqualTo("Honda");
        assertThat(v1.getModel()).isEqualTo("Civic");
        assertThat(v1.getYear()).isEqualTo(2023);
        assertThat(v1.getColor()).isEqualTo("Preto");

        assertThat(v2).isEqualTo(v2);
        assertThat(v2).isEqualTo(v3);
        assertThat(v2).isNotEqualTo(v4);
        assertThat(v2).isNotEqualTo(null);
        assertThat(v2).isNotEqualTo("String");
        assertThat(v2.hashCode()).isEqualTo(v3.hashCode());
    }

    @Test
    @DisplayName("Deve testar getters, construtores, equals e hashCode de ServiceItem")
    void testServiceItemEqualsAndHashCode() {
        ServiceItem s1 = new ServiceItem(null, "Alinhamento", "Alinhamento 3D", new BigDecimal("100.00"), 1.5);
        ServiceItem s2 = new ServiceItem("s1", "Alinhamento", "Alinhamento 3D", new BigDecimal("100.00"), 1.5);
        ServiceItem s3 = new ServiceItem("s1", "Alinhamento", "Alinhamento 3D", new BigDecimal("100.00"), 1.5);
        ServiceItem s4 = new ServiceItem("s2", "Alinhamento", "Alinhamento 3D", new BigDecimal("100.00"), 1.5);

        assertThat(s1.getId()).isNotBlank();
        assertThat(s1.getName()).isEqualTo("Alinhamento");
        assertThat(s1.getDescription()).isEqualTo("Alinhamento 3D");
        assertThat(s1.getPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(s1.getEstimatedHours()).isEqualTo(1.5);

        assertThat(s2).isEqualTo(s2);
        assertThat(s2).isEqualTo(s3);
        assertThat(s2).isNotEqualTo(s4);
        assertThat(s2).isNotEqualTo(null);
        assertThat(s2).isNotEqualTo("String");
        assertThat(s2.hashCode()).isEqualTo(s3.hashCode());
    }

    @Test
    @DisplayName("Deve testar getters, construtores, equals e hashCode de PartItem")
    void testPartItemEqualsAndHashCode() {
        PartItem p1 = new PartItem(null, "Pneu", "PN-17", 4, new BigDecimal("400.00"));
        PartItem p2 = new PartItem("p1", "Pneu", "PN-17", 4, new BigDecimal("400.00"));
        PartItem p3 = new PartItem("p1", "Pneu", "PN-17", 4, new BigDecimal("400.00"));
        PartItem p4 = new PartItem("p2", "Pneu", "PN-17", 4, new BigDecimal("400.00"));

        assertThat(p1.getId()).isNotBlank();
        assertThat(p1.getName()).isEqualTo("Pneu");
        assertThat(p1.getPartNumber()).isEqualTo("PN-17");
        assertThat(p1.getQuantity()).isEqualTo(4);
        assertThat(p1.getUnitPrice()).isEqualTo(new BigDecimal("400.00"));
        assertThat(p1.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1600.00"));

        assertThat(p2).isEqualTo(p2);
        assertThat(p2).isEqualTo(p3);
        assertThat(p2).isNotEqualTo(p4);
        assertThat(p2).isNotEqualTo(null);
        assertThat(p2).isNotEqualTo("String");
        assertThat(p2.hashCode()).isEqualTo(p3.hashCode());
    }

    @Test
    @DisplayName("Deve validar equals, hashCode e construtores com id/datas nulas em ServiceOrder")
    void testServiceOrderEqualsAndNullConstructorBranches() {
        Customer customer = Customer.create("Maria", "maria@email.com", "11988887777");
        Vehicle vehicle = Vehicle.create("XYZ-9999", "Honda", "Civic", 2023, "Preto");

        ServiceOrder osNullIdAndDates = new ServiceOrder(
                null, "OS-100", ServiceOrderStatus.RECEIVED,
                customer, vehicle, null, null,
                null, "Obs", false, null, null
        );

        assertThat(osNullIdAndDates.getId()).isNotBlank();
        assertThat(osNullIdAndDates.getCreatedAt()).isNotNull();
        assertThat(osNullIdAndDates.getUpdatedAt()).isNotNull();
        assertThat(osNullIdAndDates.getServices()).isEmpty();
        assertThat(osNullIdAndDates.getParts()).isEmpty();

        LocalDateTime now = LocalDateTime.now();
        ServiceOrder osNonNull = new ServiceOrder(
                osNullIdAndDates.getId(), "OS-100", ServiceOrderStatus.RECEIVED,
                customer, vehicle, List.of(), List.of(),
                null, "Obs", false, now, now
        );

        assertThat(osNullIdAndDates).isEqualTo(osNullIdAndDates);
        assertThat(osNullIdAndDates).isEqualTo(osNonNull);
        assertThat(osNullIdAndDates).isNotEqualTo(null);
        assertThat(osNullIdAndDates).isNotEqualTo("String");
        assertThat(osNullIdAndDates.hashCode()).isEqualTo(osNonNull.hashCode());
    }

    @Test
    @DisplayName("Deve testar todas as transições do ServiceOrderStatus enum")
    void testServiceOrderStatusTransitions() {
        for (ServiceOrderStatus status : ServiceOrderStatus.values()) {
            assertThat(status.getDescription()).isNotBlank();

            if (status != ServiceOrderStatus.DELIVERED) {
                assertThat(status.canTransitionTo(ServiceOrderStatus.CANCELED)).isTrue();
            } else {
                assertThat(status.canTransitionTo(ServiceOrderStatus.CANCELED)).isFalse();
            }
        }

        assertThat(ServiceOrderStatus.RECEIVED.canTransitionTo(ServiceOrderStatus.DIAGNOSIS)).isTrue();
        assertThat(ServiceOrderStatus.DIAGNOSIS.canTransitionTo(ServiceOrderStatus.AWAITING_APPROVAL)).isTrue();
        assertThat(ServiceOrderStatus.AWAITING_APPROVAL.canTransitionTo(ServiceOrderStatus.EXECUTION)).isTrue();
        assertThat(ServiceOrderStatus.AWAITING_APPROVAL.canTransitionTo(ServiceOrderStatus.DIAGNOSIS)).isTrue();
        assertThat(ServiceOrderStatus.EXECUTION.canTransitionTo(ServiceOrderStatus.FINISHED)).isTrue();
        assertThat(ServiceOrderStatus.FINISHED.canTransitionTo(ServiceOrderStatus.DELIVERED)).isTrue();
        assertThat(ServiceOrderStatus.DELIVERED.canTransitionTo(ServiceOrderStatus.FINISHED)).isFalse();
        assertThat(ServiceOrderStatus.CANCELED.canTransitionTo(ServiceOrderStatus.RECEIVED)).isFalse();
    }

    @Test
    @DisplayName("Deve testar exceções de domínio")
    void testDomainExceptions() {
        ServiceOrderNotFoundException ex1 = new ServiceOrderNotFoundException("123");
        assertThat(ex1.getMessage()).contains("123");

        InvalidStatusTransitionException ex2 = new InvalidStatusTransitionException("Mensagem de erro");
        assertThat(ex2.getMessage()).isEqualTo("Mensagem de erro");

        ResourceNotFoundException ex3 = new ResourceNotFoundException("Recurso não encontrado");
        assertThat(ex3.getMessage()).isEqualTo("Recurso não encontrado");
    }

    @Test
    @DisplayName("Deve testar GlobalExceptionHandler")
    void testGlobalExceptionHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ProblemDetail pd1 = handler.handleNotFound(new ServiceOrderNotFoundException("123"));
        assertThat(pd1.getStatus()).isEqualTo(404);

        ProblemDetail pd2 = handler.handleInvalidTransition(new InvalidStatusTransitionException("Transição inválida"));
        assertThat(pd2.getStatus()).isEqualTo(422);
    }
}
