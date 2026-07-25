# Terraform — Infraestrutura como Código (IaC)

Provisionamento completo da infraestrutura do sistema de Ordens de Serviço
usando Terraform com o provider Kubernetes — funciona com cluster local (Docker Desktop).

---

##  Estrutura

```
infra/
├── main.tf              # Ponto de entrada — chama os módulos
├── variables.tf         # Todas as variáveis de entrada
├── outputs.tf           # O que é exibido após o apply
├── terraform.tfvars     # Valores para ambiente local
└── modules/
    ├── networking/      # Namespace do Kubernetes
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── mysql/           # Banco de dados completo
    │   ├── main.tf      # Secret + PVC + Deployment + Service
    │   ├── variables.tf
    │   └── outputs.tf
    └── app/             # Aplicação Spring Boot
        ├── main.tf      # ConfigMap + Secret + Deployment + Service + HPA
        ├── variables.tf
        └── outputs.tf
```

---

## Recursos criados

### Módulo `networking`
| Recurso | Tipo K8s | O que faz |
|---------|----------|-----------|
| `fiap-oficina` | Namespace | Isola todos os recursos do projeto |

### Módulo `mysql`
| Recurso | Tipo K8s | O que faz |
|---------|----------|-----------|
| `mysql-secrets` | Secret | Armazena senhas do MySQL com segurança |
| `mysql-pvc` | PersistentVolumeClaim | Disco de 5Gi para os dados do banco |
| `mysql` | Deployment | Container MySQL 8.2 com health checks |
| `mysql` | Service (ClusterIP) | Ponto de acesso interno na porta 3306 |

### Módulo `app`
| Recurso | Tipo K8s | O que faz |
|---------|----------|-----------|
| `oficina-config` | ConfigMap | Variáveis não sensíveis (host, porta, url) |
| `oficina-secrets` | Secret | Senha do banco e API key do Resend |
| `oficina-app` | Deployment | App Spring Boot com 2 réplicas e probes |
| `oficina-service` | Service (ClusterIP) | Expõe a app na porta 80 internamente |
| `oficina-hpa` | HorizontalPodAutoscaler | Escala de 2 a 10 pods por CPU/memória |

**Total: 10 recursos Kubernetes provisionados via Terraform**

---

## Pré-requisitos

```bash
# 1. Terraform instalado
brew install terraform      

terraform version


kubectl get nodes

docker build -t tech-challenge-fase2:latest .
```

---

## Como aplicar — Passo a passo

### Passo 1 — Configure as variáveis sensíveis

```bash
export TF_VAR_mysql_root_password="root_password"
export TF_VAR_mysql_password="oficina_pass"
export TF_VAR_resend_api_key="re_sua_chave_real"
```

### Passo 2 — Entre na pasta infra

```bash
cd infra/
```

### Passo 3 — Inicialize o Terraform

Baixa o provider do Kubernetes (feito apenas uma vez):

```bash
terraform init
```

Saída esperada:
```
Initializing the backend...
Initializing modules...
Initializing provider plugins...
- Finding hashicorp/kubernetes versions matching "~> 2.27"...
- Installed hashicorp/kubernetes v2.27.0

Terraform has been successfully initialized!
```

### Passo 4 — Visualize o que será criado

```bash
terraform plan
```

Mostra tudo que será criado ANTES de criar. Saída esperada:
```
Plan: 10 to add, 0 to change, 0 to destroy.
```

### Passo 5 — Aplique a infraestrutura

```bash
terraform apply
```

Digite `yes` quando solicitado. Ou para pular a confirmação:

```bash
terraform apply -auto-approve
```

Saída esperada ao final:
```
Apply complete! Resources: 10 added, 0 changed, 0 destroyed.

Outputs:

namespace        = "fiap-oficina"
mysql_service    = "mysql"
app_service      = "oficina-service"
como_acessar     = "kubectl port-forward service/oficina-service 8080:80 -n fiap-oficina"
swagger_url      = "http://localhost:8080/api/swagger-ui.html"
```

### Passo 6 — Acesse a aplicação

```bash
kubectl port-forward service/oficina-service 8080:80 -n fiap-oficina
```

Acesse: http://localhost:8080/api/swagger-ui.html

---

## Operações do dia a dia

```bash
terraform show

terraform output

terraform apply

terraform destroy
```

---

## Verificar o que foi criado

```bash
kubectl get all -n fiap-oficina

kubectl get pods -n fiap-oficina

kubectl get hpa -n fiap-oficina

kubectl get configmap -n fiap-oficina
kubectl get secret -n fiap-oficina
```

---
