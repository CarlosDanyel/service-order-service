package com.fiap.tech_challenge_fase2.infrastructure.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

public class ServiceOrderEvents {

    public record ServiceOrderCreatedEvent(
            String serviceOrderId,
            String orderNumber,
            String customerName,
            String customerEmail,
            String vehicleInfo,
            String status
    ) implements Serializable {}

    public record QuotationCreatedEvent(
            String serviceOrderId,
            String orderNumber,
            String customerName,
            String customerEmail,
            String vehicleInfo,
            BigDecimal totalAmount,
            String approvalToken
    ) implements Serializable {}

    public record PaymentApprovedEvent(
            String serviceOrderId,
            String externalPaymentId
    ) implements Serializable {}

    public record PaymentFailedEvent(
            String serviceOrderId,
            String reason
    ) implements Serializable {}
}
