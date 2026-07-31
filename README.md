# Service Order Microservice (`ordem-de-service`)

Serviço responsável pela abertura, diagnóstico, atualização de status e acompanhamento do histórico das Ordens de Serviço (OS).

---

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3 |
| Build | Maven |
| Banco de Dados | MySQL 8 |
| Mensageria | RabbitMQ (Topic Exchange) |
| Testes | JUnit 5, Mockito, Cucumber (BDD), JaCoCo |
| Container | Docker, Kubernetes |
| CI/CD | GitHub Actions, SonarQube |
| Documentação | Swagger (OpenAPI 3) |

---

## Arquitetura do Serviço

Desenvolvido seguindo os princípios de **Clean Architecture** e **DDD (Domain-Driven Design)**:

- **Domain Layer (`domain`)**: Contém a entidade agregadora `ServiceOrder`, enums de status (`RECEIVED`, `DIAGNOSIS`, `AWAITING_APPROVAL`, `EXECUTION`, `FINISHED`, `DELIVERED`, `CANCELED`) e regras de transição.
- **Application Layer (`application`)**: Implementa os Casos de Uso (`CreateServiceOrderUseCase`, `UpdateServiceOrderStatusUseCase`, `ApproveQuotationUseCase`).
- **Infrastructure Layer (`infrastructure`)**: Adaptador JPA de persistência em banco relacional MySQL e publicadores/listeners RabbitMQ para o **Saga Pattern Coreografado**.

```
com.fiap.tech_challenge_fase2/
├── application/       # Use Cases, Ports (In/Out), DTOs
├── domain/            # Entities, Aggregates, Enums, Exceptions
└── infrastructure/    # Persistence (JPA/MySQL), Messaging (RabbitMQ Saga)
```

---

## Saga Pattern: Coreografado (Choreographed Saga)

### Por que Coreografado e não Orquestrado?

| Critério | Coreografado (✅ escolhido) | Orquestrado |
|---|---|---|
| **Acoplamento** | Baixo — cada serviço só conhece eventos | Alto — orquestrador conhece todos os serviços |
| **Resiliência** | Cada serviço falha independentemente | Orquestrador é single point of failure |
| **Complexidade** | Distribuída entre serviços | Concentrada no orquestrador |
| **Escalabilidade** | Serviços escalam independentemente | Orquestrador pode virar gargalo |
| **Observabilidade** | Rastreamento por correlation ID nos eventos | Centralizado no orquestrador |
| **Ideal para** | Fluxos bem definidos com poucos participantes (3-4 serviços) | Fluxos complexos com muitas ramificações condicionais |

**Justificativa:** O sistema tem 3 microsserviços com um fluxo linear bem definido (OS → Orçamento → Pagamento → Entrega). O modelo coreografado elimina o ponto único de falha de um orquestrador central, reduz o acoplamento entre serviços e se alinha ao princípio de autonomia dos microsserviços. A comunicação é totalmente assíncrona via RabbitMQ Topic Exchange (`oficina.exchange`).

### Fluxo do Saga

```
1. ordem-de-service: abre OS (RECEIVED)
   → publica ServiceOrderCreatedEvent

2. ordem-de-service: gera orçamento (AWAITING_APPROVAL)
   → publica QuotationCreatedEvent

3. notification-service: consome eventos → envia e-mails ao cliente

4. Cliente aprova/recusa via link mágico → ordem-de-service processa

5. payment-billing: gera PIX (Mercado Pago) → webhook processa pagamento
   → publica PaymentApprovedEvent ou PaymentFailedEvent

6. ordem-de-service: consome resultado do pagamento
   → DELIVERED (sucesso) ou CANCELED (compensação/rollback)
```

### Compensação (Rollback)

Quando o pagamento falha (`PaymentFailedEvent`), o **ordem-de-service** executa a ação compensatória: transiciona a OS para `CANCELED`. Este é o mecanismo de rollback do Saga — garantindo que o sistema retorne a um estado consistente em caso de falha em qualquer etapa.

---

## Documentação da API (Swagger)

A documentação interativa da API está disponível via Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## Coleção Postman

O arquivo da coleção completa de testes de API do Postman está disponível na raiz deste repositório:

- [postman_collection.json](./postman_collection.json)

A coleção contém o fluxo completo do Saga Pattern coreografado:
- **Happy Path**: OS → Diagnóstico → Orçamento → Aprovação → Pagamento PIX → Entrega
- **Rollback**: Falha de pagamento → Compensação (OS → CANCELED)

**Importe no Postman e execute as requests na ordem das pastas.**

---

## Evidências de Cobertura de Testes

A cobertura de testes é verificada via **JaCoCo** com o mínimo de **80%** exigido no pipeline de CI.

![Cobertura de Testes - JaCoCo](.docs/coverage.png)

### Executar Testes

```bash
# Executar todos os testes (unitários + BDD)
./mvnw clean test

# Executar testes com verificação de cobertura (JaCoCo ≥ 80%)
./mvnw clean verify

# Relatório de cobertura (HTML)
open target/site/jacoco/index.html
```

### CI/CD

| Pipeline | Trigger | O que faz |
|---|---|---|
| **CI** | Push e Pull Request | Build + Testes + JaCoCo + SonarQube |
| **CD** | Push na `main` | Docker build + Deploy K8s + Rollback automático |

---

## Inicialização do Projeto (Infraestrutura)

A inicialização completa da infraestrutura (subida de bancos de dados, RabbitMQ, e demais dependências) está documentada no repositório:

🔗 **[oficina-infra](https://github.com/CarlosDanyel/oficina-infra)**

Consulte o README do `oficina-infra` para instruções detalhadas de como provisionar o ambiente local e realizar o deploy completo no Kubernetes.

---

## Como Executar o Microsserviço Localmente

### Pré-requisitos:
- Java 17 e Maven instalados.
- Infraestrutura provisionada conforme o [oficina-infra](https://github.com/CarlosDanyel/oficina-infra): MySQL na porta `3307` e RabbitMQ na porta `5672`.

### Comandos:

```bash
# Executar a suíte de testes unitários e BDD (Cucumber)
./mvnw clean test

# Subir a aplicação Spring Boot
./mvnw spring-boot:run
```

### Deploy rápido (K8s)

```bash
# 1. Build da imagem
docker build -t service-order-service:latest .

# 2. Deploy no Kubernetes
kubectl apply -k k8s/

# 3. Acessar localmente
kubectl port-forward svc/service-order-service 8080:80 -n fiap-oficina
```
