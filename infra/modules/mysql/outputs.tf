output "service_name" {
  description = "Nome do Service do MySQL — usado pela aplicação como DB_HOST."
  value       = kubernetes_service.mysql.metadata[0].name
}
