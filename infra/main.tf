terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.27"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = var.kube_context
}

module "networking" {
  source    = "./modules/networking"
  namespace = var.namespace
}

module "mysql" {
  source    = "./modules/mysql"
  namespace = module.networking.namespace_name

  mysql_root_password = var.mysql_root_password
  mysql_database      = var.mysql_database
  mysql_user          = var.mysql_user
  mysql_password      = var.mysql_password
  storage_class       = var.storage_class
  storage_size        = var.mysql_storage_size

  depends_on = [module.networking]
}

module "app" {
  source    = "./modules/app"
  namespace = module.networking.namespace_name

  docker_image      = var.docker_image
  docker_tag        = var.docker_tag
  replicas          = var.app_replicas
  app_base_url      = var.app_base_url
  resend_api_key    = var.resend_api_key
  resend_from_email = var.resend_from_email

  db_host     = module.mysql.service_name
  db_port     = "3306"
  db_name     = var.mysql_database
  db_username = var.mysql_user
  db_password = var.mysql_password

  depends_on = [module.mysql]
}
