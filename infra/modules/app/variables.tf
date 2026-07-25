variable "namespace"         { type = string }
variable "docker_image"      { type = string }
variable "docker_tag"        { type = string }
variable "replicas"          { type = number }
variable "app_base_url"      { type = string }
variable "resend_from_email" { type = string }
variable "db_host"           { type = string }
variable "db_port"           { type = string }
variable "db_name"           { type = string }
variable "db_username"       { type = string }

variable "resend_api_key" {
  type      = string
  sensitive = true
}

variable "db_password" {
  type      = string
  sensitive = true
}