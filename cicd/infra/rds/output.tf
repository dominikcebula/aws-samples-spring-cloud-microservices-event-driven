output "db_endpoint" {
  value = aws_rds_cluster.aurora_cluster.endpoint
}

output "db_port" {
  value = aws_rds_cluster.aurora_cluster.port
}

output "db_name" {
  value = aws_rds_cluster.aurora_cluster.database_name
}

output "db_master_username" {
  value = aws_rds_cluster.aurora_cluster.master_username
}

output "db_master_password" {
  value = jsondecode(data.aws_secretsmanager_secret_version.postgres_password.secret_string)["password"]
}
