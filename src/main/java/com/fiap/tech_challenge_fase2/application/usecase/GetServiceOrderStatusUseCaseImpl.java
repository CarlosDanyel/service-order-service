package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.port.in.GetServiceOrderStatusUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.exception.ServiceOrderNotFoundException;

public class GetServiceOrderStatusUseCaseImpl implements GetServiceOrderStatusUseCase {

    private final ServiceOrderRepositoryPort repository;

    public GetServiceOrderStatusUseCaseImpl(ServiceOrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ServiceOrder execute(String serviceOrderId) {
        return repository.findById(serviceOrderId)
                .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));
    }
}
