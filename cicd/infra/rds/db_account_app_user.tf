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
  name        = "RdsDbAppUserConnectPolicy"
  description = "IAM policy allowing connection to RDS DB using ${postgresql_role.app_user.name} account."
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "rds-db:connect"
        ]
        Resource = [
          "arn:aws:rds-db:${var.region}:${var.aws_account_id}:dbuser:*/${postgresql_role.app_user.name}"
        ]
      }
    ]
  })
}

resource "aws_iam_policy_attachment" "db_app_user_policy_attachment" {
  name       = aws_iam_policy.db_app_user_policy.name
  roles = var.service_accounts_roles
  policy_arn = aws_iam_policy.db_app_user_policy.arn
}
