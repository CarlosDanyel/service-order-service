output "service_name" {
  description = "Nome do Service da aplicação."
  value       = kubernetes_service.app.metadata[0].name
}

output "deployment_name" {
  description = "Nome do Deployment da aplicação."
  value       = kubernetes_deployment.app.metadata[0].name
}
