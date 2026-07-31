# Kubernetes — service-order-service

Manifests para deploy do microsserviço de Ordens de Serviço no Kubernetes.

---

## Estrutura

```
k8s/
├── 00-namespace.yaml       # Namespace fiap-oficina
├── .env                    # Variáveis de ambiente e segredos (ConfigMap e Secret)
├── .env.example            # Modelo com todas as variáveis
├── kustomization.yaml      # Kustomize (gera ConfigMap/Secret a partir do .env e aplica tudo)
└── app/
    ├── 04-deployment.yaml  # Deployment com 2 réplicas, probes, anti-affinity
    ├── 05-service.yaml     # Service ClusterIP (porta 80 → 8080)
    └── 06-hpa.yaml         # HPA (CPU 70% / RAM 80%, min 2, max 10)
```

> **Infra compartilhada:** MySQL, RabbitMQ e MongoDB são gerenciados pelo repositório `oficina-infra`. Este repositório contém apenas os manifests do próprio microsserviço.

---

## Configuração (ANTES de aplicar)

Certifique-se de que o arquivo `.env` existe na pasta `k8s/`:

```bash
# Criar o .env a partir do exemplo se ainda não existir:
cp k8s/.env.example k8s/.env
```

---

## Deploy

```bash
# Aplicar todos os manifests via Kustomize
kubectl apply -k k8s/
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
