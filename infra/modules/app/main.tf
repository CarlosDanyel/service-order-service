resource "kubernetes_config_map" "app" {
  metadata {
    name      = "oficina-config"
    namespace = var.namespace
    labels    = { app = "tech-challenge-fase2" }
  }

  data = {
    DB_HOST                         = var.db_host
    DB_PORT                         = var.db_port
    DB_NAME                         = var.db_name
    DB_USERNAME                     = var.db_username
    APP_BASE_URL                    = var.app_base_url
    RESEND_FROM_EMAIL               = var.resend_from_email
    SPRING_JPA_SHOW_SQL             = "false"
    SPRING_JPA_HIBERNATE_DDL_AUTO   = "validate"
    LOGGING_LEVEL_COM_FIAP          = "INFO"
  }
}

resource "kubernetes_secret" "app" {
  metadata {
    name      = "oficina-secrets"
    namespace = var.namespace
    labels    = { app = "tech-challenge-fase2" }
  }

  type = "Opaque"

  data = {
    DB_PASSWORD    = var.db_password
    RESEND_API_KEY = var.resend_api_key
  }
}

resource "kubernetes_deployment" "app" {
  metadata {
    name      = "oficina-app"
    namespace = var.namespace
    labels = {
      app     = "tech-challenge-fase2"
      version = "1.0.0"
    }
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = { app = "tech-challenge-fase2" }
    }

    # RollingUpdate: zero downtime — sobe novos antes de derrubar antigos
    strategy {
      type = "RollingUpdate"
      rolling_update {
        max_surge       = "1"
        max_unavailable = "0"
      }
    }

    template {
      metadata {
        labels = {
          app     = "tech-challenge-fase2"
          version = "1.0.0"
        }
      }

      spec {
        # Distribui pods em nodes diferentes para alta disponibilidade
        affinity {
          pod_anti_affinity {
            preferred_during_scheduling_ignored_during_execution {
              weight = 100
              pod_affinity_term {
                label_selector {
                  match_expressions {
                    key      = "app"
                    operator = "In"
                    values   = ["tech-challenge-fase2"]
                  }
                }
                topology_key = "kubernetes.io/hostname"
              }
            }
          }
        }

        container {
          name  = "oficina-app"
          image = "${var.docker_image}:${var.docker_tag}"

          # IfNotPresent: usa imagem local se disponível (essencial para local)
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8080
            name           = "http"
          }

          # Injeta todas as variáveis do ConfigMap
          env_from {
            config_map_ref {
              name = kubernetes_config_map.app.metadata[0].name
            }
          }

          # Injeta variáveis sensíveis do Secret individualmente
          env {
            name = "DB_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app.metadata[0].name
                key  = "DB_PASSWORD"
              }
            }
          }
          env {
            name = "RESEND_API_KEY"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.app.metadata[0].name
                key  = "RESEND_API_KEY"
              }
            }
          }

          # Recursos — requests é a base do HPA para calcular % de uso
          resources {
            requests = {
              cpu    = "250m"
              memory = "512Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "1Gi"
            }
          }

          # Startup Probe — dá tempo para o Flyway rodar as migrations
          startup_probe {
            http_get {
              path = "/api/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            failure_threshold     = 12   # até 150s para iniciar
          }

          # Readiness Probe — só recebe tráfego quando estiver pronto
          readiness_probe {
            http_get {
              path = "/api/actuator/health/readiness"
              port = 8080
            }
            initial_delay_seconds = 45
            period_seconds        = 10
            failure_threshold     = 3
          }

          # Liveness Probe — reinicia se travar
          liveness_probe {
            http_get {
              path = "/api/actuator/health/liveness"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 15
            failure_threshold     = 3
          }
        }

        # Shutdown gracioso — espera requests em andamento terminarem
        termination_grace_period_seconds = 30
      }
    }
  }
}

# ── 4. Service — expõe a app dentro do cluster ───────────────────────────────
resource "kubernetes_service" "app" {
  metadata {
    name      = "oficina-service"
    namespace = var.namespace
    labels    = { app = "tech-challenge-fase2" }
  }

  spec {
    type = "ClusterIP"

    selector = {
      app = "tech-challenge-fase2"
    }

    port {
      name        = "http"
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
  }
}

# ── 5. HPA — escala automaticamente por CPU e memória ────────────────────────
resource "kubernetes_horizontal_pod_autoscaler_v2" "app" {
  metadata {
    name      = "oficina-hpa"
    namespace = var.namespace
  }

  spec {
    min_replicas = 2
    max_replicas = 10

    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = kubernetes_deployment.app.metadata[0].name
    }

    # Escala quando CPU > 70%
    metric {
      type = "Resource"
      resource {
        name = "cpu"
        target {
          type                = "Utilization"
          average_utilization = 70
        }
      }
    }

    # Escala quando memória > 80%
    metric {
      type = "Resource"
      resource {
        name = "memory"
        target {
          type                = "Utilization"
          average_utilization = 80
        }
      }
    }

    behavior {
      # Scale UP rápido
      scale_up {
        stabilization_window_seconds = 60
        select_policy                = "Max"
        policy {
          type          = "Pods"
          value         = 2
          period_seconds = 60
        }
      }

      # Scale DOWN devagar (evita flapping)
      scale_down {
        stabilization_window_seconds = 300
        select_policy                = "Min"
        policy {
          type          = "Pods"
          value         = 1
          period_seconds = 120
        }
      }
    }
  }
}
