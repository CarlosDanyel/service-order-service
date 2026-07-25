package com.fiap.tech_challenge_fase2.application.port.out;

import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

public interface EmailNotificationGateway {

    void sendQuotationApprovalEmail(ServiceOrder serviceOrder);

    void sendStatusUpdateEmail(ServiceOrder serviceOrder);
}
