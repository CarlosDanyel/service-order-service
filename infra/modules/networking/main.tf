
resource "kubernetes_namespace" "fiap" {
  metadata {
    name = var.namespace

    labels = {
      app         = "tech-challenge-fase2"
      environment = "local"
      managed-by  = "terraform"
    }
  }
}
