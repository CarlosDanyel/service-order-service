# Service Order Microservice (`service-order-service`)

Serviço responsável pela abertura, diagnóstico, atualização de status e acompanhamento do histórico das Ordens de Serviço (OS).

---

## Arquitetura do Serviço

Desenvolvido seguindo os princípios de Clean Architecture e DDD (Domain-Driven Design):

- **Domain Layer (`domain`)**: Contém a entidade agregadora `ServiceOrder`, enums de status (`RECEIVED`, `DIAGNOSIS`, `AWAITING_APPROVAL`, `EXECUTION`, `FINISHED`, `DELIVERED`, `CANCELED`) e regras de transição.
- **Application Layer (`application`)**: Implementa os Casos de Uso (`CreateServiceOrderUseCase`, `UpdateServiceOrderStatusUseCase`, `ApproveQuotationUseCase`).
- **Infrastructure Layer (`infrastructure`)**: Adaptador JPA de persistência em banco relacional MySQL (`oficina_db`) e publicadores/listeners RabbitMQ para o **Saga Pattern Coreografado**.

```text
com.fiap.tech_challenge_fase2/
├── application/       # Use Cases, Ports (In/Out), DTOs
├── domain/            # Entities, Aggregates, Enums, Exceptions
└── infrastructure/    # Persistence (JPA/MySQL), Messaging (RabbitMQ Saga)
```

---

## Coleção do Postman

O arquivo da coleção completa de testes de API do Postman está disponível na raiz deste repositório:
- [postman_collection.json](./postman_collection.json)

---

## Como Executar o Microsserviço Localmente

### Pré-requisitos:
- Java 17 e Maven instalados.
- Banco MySQL rodando na porta `3307` e RabbitMQ na porta `5672` (via Docker Compose).

### Comandos:

```bash
# Executar a suíte de testes unitários e BDD (Cucumber)
./mvnw clean test

# Subir a aplicação Spring Boot
./mvnw spring-boot:run
```

- **Swagger UI**: http://localhost:8080/swagger-ui.html
