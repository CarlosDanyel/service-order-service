package com.fiap.tech_challenge_fase2.domain.exception;

public class ServiceOrderNotFoundException extends RuntimeException {
    public ServiceOrderNotFoundException(String id) {
        super("Ordem de Serviço não encontrada: " + id);
    }
}
