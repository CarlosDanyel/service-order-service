package com.fiap.tech_challenge_fase2.messaging;

import com.fiap.tech_challenge_fase2.infrastructure.messaging.EventPublisher;
import com.fiap.tech_challenge_fase2.infrastructure.messaging.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("Deve publicar evento no RabbitMQ com sucesso")
    void shouldPublishEventSuccessfully() {
        String routingKey = "service-order.created";
        Object dummyEvent = new Object();

        eventPublisher.publishEvent(routingKey, dummyEvent);

        verify(rabbitTemplate, times(1)).convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, dummyEvent);
    }

    @Test
    @DisplayName("Deve capturar exceção e logar erro quando falhar ao publicar no RabbitMQ")
    void shouldHandleExceptionWhenPublishFails() {
        String routingKey = "service-order.created";
        Object dummyEvent = new Object();

        doThrow(new RuntimeException("RabbitMQ indisponível"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Não deve lançar exceção para a aplicação
        eventPublisher.publishEvent(routingKey, dummyEvent);

        verify(rabbitTemplate, times(1)).convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, dummyEvent);
    }
}
