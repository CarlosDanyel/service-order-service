package com.fiap.tech_challenge_fase2.domain.enums;

public enum ServiceOrderStatus {

    RECEIVED("Recebida"),
    DIAGNOSIS("Diagnóstico"),
    AWAITING_APPROVAL("Aguardando Aprovação"),
    EXECUTION("Em Execução"),
    FINISHED("Finalizada"),
    DELIVERED("Entregue");

    private final String description;

    ServiceOrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(ServiceOrderStatus next) {
        return switch (this) {
            case RECEIVED          -> next == DIAGNOSIS;
            case DIAGNOSIS         -> next == AWAITING_APPROVAL;
            case AWAITING_APPROVAL -> next == EXECUTION || next == DIAGNOSIS;
            case EXECUTION         -> next == FINISHED;
            case FINISHED          -> next == DELIVERED;
            case DELIVERED         -> false;
        };
    }
}
