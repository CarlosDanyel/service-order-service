package com.fiap.tech_challenge_fase2.application.port.out;

import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepositoryPort {

    ServiceOrder save(ServiceOrder serviceOrder);

    Optional<ServiceOrder> findById(String id);

    Optional<ServiceOrder> findByApprovalToken(String token);

    List<ServiceOrder> findActiveOrdered();
}
