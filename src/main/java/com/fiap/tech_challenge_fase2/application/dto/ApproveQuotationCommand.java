package com.fiap.tech_challenge_fase2.application.dto;

public record ApproveQuotationCommand(
        String  serviceOrderId,
        String  token,
        boolean approved
) {}
