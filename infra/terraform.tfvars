
kube_context  = "docker-desktop"
namespace     = "fiap-oficina"
storage_class = "hostpath"

docker_image = "tech-challenge-fase2"
docker_tag   = "latest"
app_replicas = 2
app_base_url = "http://localhost:8080"

resend_from_email = "oficina@fabrincahub.com"

mysql_database     = "oficina_db"
mysql_user         = "oficina_user"
mysql_storage_size = "5Gi"
