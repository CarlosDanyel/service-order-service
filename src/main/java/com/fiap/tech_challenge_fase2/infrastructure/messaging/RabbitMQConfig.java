package com.fiap.tech_challenge_fase2.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "oficina.exchange";

    public static final String QUEUE_SERVICE_ORDER_CREATED = "service-order.created.queue";
    public static final String QUEUE_QUOTATION_CREATED     = "quotation.created.queue";
    public static final String QUEUE_PAYMENT_APPROVED      = "payment.approved.queue";
    public static final String QUEUE_PAYMENT_FAILED        = "payment.failed.queue";

    public static final String ROUTING_KEY_OS_CREATED      = "service-order.created";
    public static final String ROUTING_KEY_QUOTATION       = "quotation.created";
    public static final String ROUTING_KEY_PAY_APPROVED    = "payment.approved";
    public static final String ROUTING_KEY_PAY_FAILED      = "payment.failed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue osCreatedQueue() {
        return new Queue(QUEUE_SERVICE_ORDER_CREATED);
    }

    @Bean
    public Queue quotationCreatedQueue() {
        return new Queue(QUEUE_QUOTATION_CREATED);
    }

    @Bean
    public Queue paymentApprovedQueue() {
        return new Queue(QUEUE_PAYMENT_APPROVED);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(QUEUE_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindingOsCreated(Queue osCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(osCreatedQueue).to(exchange).with(ROUTING_KEY_OS_CREATED);
    }

    @Bean
    public Binding bindingQuotation(Queue quotationCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(quotationCreatedQueue).to(exchange).with(ROUTING_KEY_QUOTATION);
    }

    @Bean
    public Binding bindingPaymentApproved(Queue paymentApprovedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentApprovedQueue).to(exchange).with(ROUTING_KEY_PAY_APPROVED);
    }

    @Bean
    public Binding bindingPaymentFailed(Queue paymentFailedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(exchange).with(ROUTING_KEY_PAY_FAILED);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
