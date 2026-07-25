package com.fiap.tech_challenge_fase2.application.usecase;

import com.fiap.tech_challenge_fase2.application.port.in.ListServiceOrdersUseCase;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

import java.util.List;

public class ListServiceOrdersUseCaseImpl implements ListServiceOrdersUseCase {

    private final ServiceOrderRepositoryPort repository;

    public ListServiceOrdersUseCaseImpl(ServiceOrderRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<ServiceOrder> execute() {
        return repository.findActiveOrdered();
    }
}
