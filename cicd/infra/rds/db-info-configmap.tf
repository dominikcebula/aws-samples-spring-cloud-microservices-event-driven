resource "terraform_data" "jenkins_instance" {
  provisioner "local-exec" {
    command     = "envsubst < ${path.module}/db-info-configmap.yaml.tpl | kubectl apply -f -"
    working_dir = path.module

    environment = {
      RDS_HOSTNAME = aws_rds_cluster.aurora_cluster.endpoint
      RDS_PORT     = aws_rds_cluster.aurora_cluster.port
      RDS_DB_NAME  = aws_rds_cluster.aurora_cluster.database_name
      RDS_USERNAME = postgresql_role.app_user.name
    }
  }

  depends_on = [aws_rds_cluster.aurora_cluster, aws_rds_cluster_instance.aurora_cluster_instances]
}
