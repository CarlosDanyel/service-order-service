variable "namespace"      { type = string }
variable "mysql_database" { type = string }
variable "mysql_user"     { type = string }
variable "storage_class"  { type = string }
variable "storage_size"   { type = string }

variable "mysql_root_password" {
  type      = string
  sensitive = true
}

variable "mysql_password" {
  type      = string
  sensitive = true
}