package com.fiap.tech_challenge_fase2.infrastructure.messaging;

import com.fiap.tech_challenge_fase2.application.port.out.ServiceOrderRepositoryPort;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaEventListener {

    private static final Logger log = LoggerFactory.getLogger(SagaEventListener.class);

    private final ServiceOrderRepositoryPort repository;

    public SagaEventListener(ServiceOrderRepositoryPort repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_APPROVED)
    public void handlePaymentApproved(ServiceOrderEvents.PaymentApprovedEvent event) {
        log.info("Saga Evento Recebido - PaymentApproved: {}", event);
        repository.findById(event.serviceOrderId()).ifPresent(so -> {
            if (so.getStatus() == ServiceOrderStatus.FINISHED || so.getStatus() == ServiceOrderStatus.AWAITING_APPROVAL) {
                so.transitionTo(ServiceOrderStatus.DELIVERED);
                repository.save(so);
                log.info("SAGA SUCESSO: OS {} finalizada e entregue após pagamento aprovado.", so.getId());
            }
        });
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_FAILED)
    public void handlePaymentFailed(ServiceOrderEvents.PaymentFailedEvent event) {
        log.info("Saga Evento Recebido - PaymentFailed (Rollback/Compensação): {}", event);
        repository.findById(event.serviceOrderId()).ifPresent(so -> {
            so.transitionTo(ServiceOrderStatus.CANCELED);
            repository.save(so);
            log.warn("SAGA ROLLBACK COMPENSATÓRIO: OS {} cancelada devido a falha no pagamento. Motivo: {}", so.getId(), event.reason());
        });
    }
}
