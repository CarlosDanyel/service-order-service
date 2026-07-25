package com.fiap.tech_challenge_fase2.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEvent(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.info("Evento publicado com sucesso no RabbitMQ. RoutingKey: {}, Evento: {}", routingKey, event);
        } catch (Exception e) {
            log.error("Erro ao publicar evento no RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
