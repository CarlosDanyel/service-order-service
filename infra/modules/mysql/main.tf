
resource "kubernetes_secret" "mysql" {
  metadata {
    name      = "mysql-secrets"
    namespace = var.namespace
    labels    = { app = "mysql" }
  }

  type = "Opaque"

  data = {
    MYSQL_ROOT_PASSWORD = var.mysql_root_password
    MYSQL_PASSWORD      = var.mysql_password
  }
}

resource "kubernetes_persistent_volume_claim" "mysql" {
  metadata {
    name      = "mysql-pvc"
    namespace = var.namespace
    labels    = { app = "mysql" }
  }

  spec {
    access_modes       = ["ReadWriteOnce"]
    storage_class_name = var.storage_class

    resources {
      requests = {
        storage = var.storage_size
      }
    }
  }

  lifecycle {
    prevent_destroy = false
  }
}

resource "kubernetes_deployment" "mysql" {
  metadata {
    name      = "mysql"
    namespace = var.namespace
    labels    = { app = "mysql" }
  }

  spec {
    replicas = 1

    selector {
      match_labels = { app = "mysql" }
    }

    strategy {
      type = "Recreate"
    }

    template {
      metadata {
        labels = { app = "mysql" }
      }

      spec {
        container {
          name  = "mysql"
          image = "mysql:8.2"

          port {
            container_port = 3306
          }

          # Variáveis não sensíveis
          env {
            name  = "MYSQL_DATABASE"
            value = var.mysql_database
          }
          env {
            name  = "MYSQL_USER"
            value = var.mysql_user
          }

          # Variáveis sensíveis — vêm do Secret
          env {
            name = "MYSQL_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.mysql.metadata[0].name
                key  = "MYSQL_PASSWORD"
              }
            }
          }
          env {
            name = "MYSQL_ROOT_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret.mysql.metadata[0].name
                key  = "MYSQL_ROOT_PASSWORD"
              }
            }
          }

          volume_mount {
            name       = "mysql-storage"
            mount_path = "/var/lib/mysql"
          }

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

          liveness_probe {
            exec {
              command = ["mysqladmin", "ping", "-h", "localhost",
                "-u", "root", "-p$(MYSQL_ROOT_PASSWORD)"]
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            failure_threshold     = 3
          }

          readiness_probe {
            exec {
              command = ["mysqladmin", "ping", "-h", "localhost",
                "-u", "root", "-p$(MYSQL_ROOT_PASSWORD)"]
            }
            initial_delay_seconds = 15
            period_seconds        = 5
          }
        }

        volume {
          name = "mysql-storage"
          persistent_volume_claim {
            claim_name = kubernetes_persistent_volume_claim.mysql.metadata[0].name
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "mysql" {
  metadata {
    name      = "mysql"
    namespace = var.namespace
    labels    = { app = "mysql" }
  }

  spec {
    type = "ClusterIP"

    selector = {
      app = "mysql"
    }

    port {
      port        = 3306
      target_port = 3306
      protocol    = "TCP"
    }
  }
}
