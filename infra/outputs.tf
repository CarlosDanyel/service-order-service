output "namespace" {
  description = "Namespace do Kubernetes criado."
  value       = module.networking.namespace_name
}

output "mysql_service_name" {
  description = "Nome do Service do MySQL dentro do cluster."
  value       = module.mysql.service_name
}

output "app_service_name" {
  description = "Nome do Service da aplicação dentro do cluster."
  value       = module.app.service_name
}

output "como_acessar" {
  description = "Comando para acessar a aplicação localmente."
  value       = "kubectl port-forward service/${module.app.service_name} 8080:80 -n ${module.networking.namespace_name}"
}

output "swagger_url" {
  description = "URL do Swagger após o port-forward."
  value       = "http://localhost:8080/api/swagger-ui.html"
}
