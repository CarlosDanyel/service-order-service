output "namespace_name" {
  description = "Nome do namespace criado."
  value       = kubernetes_namespace.fiap.metadata[0].name
}
