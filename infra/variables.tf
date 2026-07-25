
variable "kube_context" {
  description = "Contexto do kubectl a ser usado. 'docker-desktop' para cluster local."
  type        = string
  default     = "docker-desktop"
}

variable "namespace" {
  description = "Namespace do Kubernetes onde todos os recursos serão criados."
  type        = string
  default     = "fiap-oficina"
}

variable "storage_class" {
  description = "StorageClass para o PersistentVolume do MySQL. 'hostpath' para Docker Desktop."
  type        = string
  default     = "hostpath"
}

variable "docker_image" {
  description = "Nome da imagem Docker da aplicação."
  type        = string
  default     = "tech-challenge-fase2"
}

variable "docker_tag" {
  description = "Tag da imagem Docker. 'latest' para desenvolvimento local."
  type        = string
  default     = "latest"
}

variable "app_replicas" {
  description = "Número inicial de réplicas da aplicação."
  type        = number
  default     = 2
}

variable "app_base_url" {
  description = "URL base da aplicação (usada nos links de e-mail de aprovação)."
  type        = string
  default     = "http://localhost:8080"
}


variable "resend_api_key" {
  description = "Chave de API do Resend para envio de e-mails."
  type        = string
  sensitive   = true
  default     = "re_placeholder"
}

variable "resend_from_email" {
  description = "E-mail remetente usado pelo Resend."
  type        = string
  default     = "oficina@fabrincahub.com"
}

variable "mysql_root_password" {
  description = "Senha do usuário root do MySQL."
  type        = string
  sensitive   = true
  default     = "root_password"
}

variable "mysql_database" {
  description = "Nome do banco de dados a ser criado."
  type        = string
  default     = "oficina_db"
}

variable "mysql_user" {
  description = "Usuário do banco de dados da aplicação."
  type        = string
  default     = "oficina_user"
}

variable "mysql_password" {
  description = "Senha do usuário do banco de dados."
  type        = string
  sensitive   = true
  default     = "oficina_pass"
}

variable "mysql_storage_size" {
  description = "Tamanho do disco persistente do MySQL."
  type        = string
  default     = "5Gi"
}
