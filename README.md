# Tech Challenge — Fase 3: Sistema Distribuído de Ordens de Serviço (Saga Pattern)

Projeto de arquitetura distribuída em microsserviços para gestão de oficinas mecânicas com múltiplas filiais, garantindo resiliência, consistência transacional e tolerância a falhas.

---

## 🛠️ Microsserviços e Arquitetura

O sistema é dividido em microsserviços especializados, cada um com seu próprio repositório, banco de dados e responsabilidade bem definida:

- **`ordem-de-service` (Service Order)**: Gestão de abertura de OS, diagnósticos, execução e atualização de status.
  - Banco de Dados: MySQL (`oficina_db`)
- **`payment-billing` (Billing Service)**: Geração de cobranças, integração PIX via Mercado Pago e Webhooks.
  - Banco de Dados: MySQL (`payment_db`)
- **`notification-service` (Notification Service)**: Disparo de e-mails via Resend API e auditoria.
  - Banco de Dados: MongoDB (`notification_db`)
- **Mensageria**: RabbitMQ (Broker AMQP para orquestração da Saga Coreografada)

---

## 🔄 Saga Pattern (Coreografia)

Optamos pela **Saga Coreografada** via eventos no RabbitMQ para evitar ponto único de falha e garantir o desacoplamento dos serviços.

### Fluxo Transacional:
1. `ordem-de-service` cria a OS e envia o evento `QuotationCreatedEvent`.
2. `notification-service` consome o evento e dispara o e-mail de aprovação.
3. `payment-billing` recebe o pagamento do Mercado Pago e emite `PaymentApprovedEvent`.
4. `ordem-de-service` consome a aprovação e finaliza a OS (`DELIVERED`).
5. **Rollback Compensatório**: Se o pagamento falhar (`PaymentFailedEvent`), a OS é automaticamente cancelada (`CANCELED`).

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados.

### Passo a Passo

1. **Estrutura de Pastas:**
   Certifique-se de que os repositórios estejam na mesma pasta pai:
   ```text
   fase-3/
   ├── oficina-infra/
   ├── ordem-de-service/
   ├── payment-billing/
   └── notification-service/
   ```

2. **Iniciar todos os Serviços:**
   Navegue até a pasta de infraestrutura e rode o comando:
   ```bash
   cd oficina-infra
   docker-compose up -d --build
   ```

3. **Verificar os Contêineres:**
   ```bash
   docker-compose ps
   ```

4. **Painéis e Documentação (Swagger):**
   - **RabbitMQ Management**: http://localhost:15672 (`guest` / `guest`)
   - **OS Service API**: http://localhost:8080/swagger-ui.html
   - **Payment Billing API**: http://localhost:8081/swagger-ui.html
   - **Notification API**: http://localhost:8082/swagger-ui.html

---

## 🧪 Testes e BDD

Para executar os testes unitários e o fluxo BDD com Cucumber:

```bash
# Executar testes no Service Order (inclui Cucumber BDD)
cd ../ordem-de-service && ./mvnw clean test

# Executar testes no Payment Billing
cd ../payment-billing && ./mvnw clean test

# Executar testes no Notification Service
cd ../notification-service && ./mvnw clean test
```
