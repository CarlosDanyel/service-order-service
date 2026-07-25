# language: pt
Funcionalidade: Gestão do Fluxo Transacional Completo da Ordem de Serviço (Saga Coreografada)

  Cenário: Abertura da Ordem de Serviço, diagnóstico, aprovação de orçamento e faturamento com sucesso
    Dado que o cliente "Carlos Danyel" abre uma OS para o veículo "ABC-1234" marca "Toyota" modelo "Corolla"
    Quando a OS é registrada no sistema
    Então a OS deve ser criada com o status "RECEIVED"
    E a OS avança para o status "DIAGNOSIS"
    E o orçamento é gerado mudando o status para "AWAITING_APPROVAL"
    E o cliente aprova o orçamento mudando o status para "EXECUTION"
    E a execução é concluída mudando o status para "FINISHED"
    E o pagamento é aprovado no serviço de billing finalizando o status da OS como "DELIVERED"

  Cenário: Rollback compensatório da Ordem de Serviço devido a falha no pagamento
    Dado que uma OS com valor de orçamento aprovado está aguardando confirmação financeira
    Quando o serviço de pagamento rejeita o pagamento da cobrança
    Então o mecanismo de Saga dispara a compensação alterando o status da OS para "CANCELED"
