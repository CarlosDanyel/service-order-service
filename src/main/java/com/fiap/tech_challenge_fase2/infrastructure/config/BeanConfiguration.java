package com.fiap.tech_challenge_fase2.infrastructure.config;

import com.fiap.tech_challenge_fase2.application.port.in.*;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.*;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter.ServiceOrderPersistenceAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.ServiceOrderJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public ServiceOrderRepositoryPort serviceOrderRepositoryPort(
            ServiceOrderJpaRepository jpaRepository) {
        return new ServiceOrderPersistenceAdapter(jpaRepository);
    }

    @Bean
    public CreateServiceOrderUseCase createServiceOrderUseCase(
            ServiceOrderRepositoryPort repo,
            com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher eventPublisher) {
        return new CreateServiceOrderUseCaseImpl(repo, eventPublisher);
    }

    @Bean
    public GetServiceOrderStatusUseCase getServiceOrderStatusUseCase(
            ServiceOrderRepositoryPort repo) {
        return new GetServiceOrderStatusUseCaseImpl(repo);
    }

    @Bean
    public ApproveQuotationUseCase approveQuotationUseCase(
            ServiceOrderRepositoryPort repo) {
        return new ApproveQuotationUseCaseImpl(repo);
    }

    @Bean
    public ListServiceOrdersUseCase listServiceOrdersUseCase(
            ServiceOrderRepositoryPort repo) {
        return new ListServiceOrdersUseCaseImpl(repo);
    }

    @Bean
    public UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase(
            ServiceOrderRepositoryPort repo,
            com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher eventPublisher) {
        return new UpdateServiceOrderStatusUseCaseImpl(repo, eventPublisher);
    }
}
