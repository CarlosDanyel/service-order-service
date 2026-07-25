package com.fiap.tech_challenge_fase2.application.port.in;

import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;

import java.util.List;

public interface ListServiceOrdersUseCase {
    List<ServiceOrder> execute();
}
