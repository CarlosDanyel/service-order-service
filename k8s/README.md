# Kubernetes — service-order-service

Manifests para deploy do microsserviço de Ordens de Serviço no Kubernetes.

---

## Estrutura

```
k8s/
├── 00-namespace.yaml       # Namespace fiap-oficina
├── 01-configmap.yaml       # ConfigMap (DB, RabbitMQ, URLs — não sensíveis)
├── 02-secret.yaml          # Secret (senhas e credenciais)
├── kustomization.yaml      # Kustomize (aplica todos os manifests)
└── app/
    ├── 04-deployment.yaml  # Deployment com 2 réplicas, probes, anti-affinity
    ├── 05-service.yaml     # Service ClusterIP (porta 80 → 8080)
    └── 06-hpa.yaml         # HPA (CPU 70% / RAM 80%, min 2, max 10)
```

> **Infra compartilhada:** MySQL, RabbitMQ e MongoDB são gerenciados pelo repositório `oficina-infra`. Este repositório contém apenas os manifests do próprio microsserviço.

---

## Pré-requisitos

```bash
# Cluster Kubernetes rodando
minikube start --cpus=4 --memory=4096

# Metrics Server (necessário para HPA)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# NGINX Ingress Controller (gerenciado pelo oficina-infra)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

---

## Deploy

```bash
# Aplicar todos os manifests
kubectl apply -k k8s/

# Ou individualmente:
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/app/04-deployment.yaml
kubectl apply -f k8s/app/05-service.yaml
kubectl apply -f k8s/app/06-hpa.yaml
```

---

## Configurar Secrets (ANTES de aplicar)

```bash
echo -n "oficina_pass" | base64   # → atualizar 02-secret.yaml
```

---

## Verificar o deploy

```bash
kubectl get all -n fiap-oficina
kubectl get pods -n fiap-oficina -w
kubectl logs -f deployment/service-order-service -n fiap-oficina
kubectl describe hpa service-order-hpa -n fiap-oficina
kubectl get hpa service-order-hpa -n fiap-oficina -w
```

---

## Acessar localmente

```bash
# Port-forward para Swagger e API
kubectl port-forward svc/service-order-service 8080:80 -n fiap-oficina
# Swagger UI: http://localhost:8080/swagger-ui.html
# API:        http://localhost:8080/api/service-orders
```

---

## HPA — Escalonamento automático

```
Carga normal (CPU < 70%, RAM < 80%) → 2 réplicas (mínimo)
Carga alta   (CPU > 70% ou RAM > 80%) → escala até 10 réplicas
  └─ adiciona 2 pods por vez, a cada 60s
Carga cai    (> 5 min abaixo dos thresholds) → reduz 1 pod a cada 2 min
```

---

## Remover tudo

```bash
kubectl delete -k k8s/
```
