resource "aws_rds_cluster" "aurora_cluster" {
  cluster_identifier                  = "aurora-cluster-01"
  engine                              = "aurora-postgresql"
  engine_mode                         = "provisioned"
  engine_version                      = "15.4"
  database_name                       = "database01"
  master_username                     = "postgres"
  manage_master_user_password         = true
  backup_retention_period             = 1
  skip_final_snapshot                 = true
  db_subnet_group_name                = aws_db_subnet_group.db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  iam_database_authentication_enabled = true
}

resource "aws_rds_cluster_instance" "aurora_cluster_instances" {
  identifier           = "aurora-cluster-01-01"
  count                = 1
  cluster_identifier   = aws_rds_cluster.aurora_cluster.id
  instance_class       = "db.t3.medium"
  engine               = aws_rds_cluster.aurora_cluster.engine
  engine_version       = aws_rds_cluster.aurora_cluster.engine_version
  publicly_accessible  = true
  db_subnet_group_name = aws_rds_cluster.aurora_cluster.db_subnet_group_name
}

resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "db-subnet-group"
  subnet_ids = var.db_subnet_ids
}

data "aws_secretsmanager_secret_version" "postgres_password" {
  secret_id = aws_rds_cluster.aurora_cluster.master_user_secret[0].secret_arn
}

resource "aws_security_group" "rds_sg" {
  description = "DB security group"

  name_prefix = "rds-db-"

  vpc_id = var.db_vpc_id

  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    cidr_blocks = concat(var.db_allow_access_from_subnets_cidr_blocks, ["${var.my_public_ip}/32"])
  }
}
