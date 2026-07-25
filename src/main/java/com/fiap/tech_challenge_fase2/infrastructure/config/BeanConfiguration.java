package com.fiap.tech_challenge_fase2.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.tech_challenge_fase2.application.port.in.*;
import com.fiap.tech_challenge_fase2.application.port.out.EmailNotificationGateway;
import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.application.usecase.*;
import com.fiap.tech_challenge_fase2.infrastructure.email.adapter.ResendEmailAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.adapter.ServiceOrderPersistenceAdapter;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.repository.ServiceOrderJpaRepository;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class BeanConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public ServiceOrderRepositoryPort serviceOrderRepositoryPort(
            ServiceOrderJpaRepository jpaRepository) {
        return new ServiceOrderPersistenceAdapter(jpaRepository);
    }

    @Bean
    public EmailNotificationGateway emailNotificationGateway(
            OkHttpClient okHttpClient,
            ObjectMapper objectMapper,
            @Value("${resend.api-key}")    String apiKey,
            @Value("${resend.from-email}") String fromEmail,
            @Value("${app.base-url}")      String appBaseUrl) {
        return new ResendEmailAdapter(okHttpClient, objectMapper, apiKey, fromEmail, appBaseUrl);
    }

    @Bean
    public CreateServiceOrderUseCase createServiceOrderUseCase(
            ServiceOrderRepositoryPort repo, EmailNotificationGateway email) {
        return new CreateServiceOrderUseCaseImpl(repo, email);
    }

    @Bean
    public GetServiceOrderStatusUseCase getServiceOrderStatusUseCase(
            ServiceOrderRepositoryPort repo) {
        return new GetServiceOrderStatusUseCaseImpl(repo);
    }

    @Bean
    public ApproveQuotationUseCase approveQuotationUseCase(
            ServiceOrderRepositoryPort repo, EmailNotificationGateway email) {
        return new ApproveQuotationUseCaseImpl(repo, email);
    }

    @Bean
    public ListServiceOrdersUseCase listServiceOrdersUseCase(
            ServiceOrderRepositoryPort repo) {
        return new ListServiceOrdersUseCaseImpl(repo);
    }

    @Bean
    public UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase(
            ServiceOrderRepositoryPort repo, EmailNotificationGateway email) {
        return new UpdateServiceOrderStatusUseCaseImpl(repo, email);
    }
}
