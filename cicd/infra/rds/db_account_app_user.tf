provider "postgresql" {
  host            = aws_rds_cluster.aurora_cluster.endpoint
  port            = aws_rds_cluster.aurora_cluster.port
  database        = aws_rds_cluster.aurora_cluster.database_name
  username        = aws_rds_cluster.aurora_cluster.master_username
  password        = jsondecode(data.aws_secretsmanager_secret_version.postgres_password.secret_string)["password"]
  sslmode         = "require"
  connect_timeout = 15
  superuser       = false
}

resource "postgresql_role" "app_user" {
  name  = "app_user"
  login = true
  roles = ["rds_iam"]
}

resource "postgresql_grant" "app_user_tables" {
  database    = aws_rds_cluster.aurora_cluster.database_name
  role        = postgresql_role.app_user.name
  schema      = "public"
  object_type = "table"
  privileges = ["SELECT", "INSERT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES", "TRIGGER"]
}

resource "postgresql_grant" "app_user_schema" {
  database    = aws_rds_cluster.aurora_cluster.database_name
  role        = postgresql_role.app_user.name
  schema      = "public"
  object_type = "schema"
  privileges = ["CREATE", "USAGE"]
}

resource "postgresql_grant" "app_user_sequences" {
  database    = aws_rds_cluster.aurora_cluster.database_name
  role        = postgresql_role.app_user.name
  schema      = "public"
  object_type = "sequence"
  privileges = ["USAGE", "SELECT", "UPDATE"]
}

resource "postgresql_grant" "app_user_connect" {
  database    = aws_rds_cluster.aurora_cluster.database_name
  role        = postgresql_role.app_user.name
  object_type = "database"
  privileges = ["CONNECT"]
}

resource "aws_iam_policy" "db_app_user_policy" {
  for_each = {for idx, instance in aws_rds_cluster_instance.aurora_cluster_instances : idx => instance}

  name = "RdsDbAppUserConnectPolicy-${each.value.dbi_resource_id}"
  description = "IAM policy for ${postgresql_role.app_user.name} to connect to RDS"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "rds-db:connect"
        ]
        Resource = [
          "arn:aws:rds-db:${var.region}:${var.aws_account_id}:dbuser:${each.value.dbi_resource_id}/${postgresql_role.app_user.name}"
        ]
      }
    ]
  })
}

resource "aws_iam_policy_attachment" "db_app_user_policy_attachment" {
  for_each = aws_iam_policy.db_app_user_policy

  name       = each.value.name
  roles = [var.role_app_runner_name]
  policy_arn = each.value.arn
}
