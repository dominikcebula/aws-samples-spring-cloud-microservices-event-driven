resource "aws_rds_cluster" "aurora_cluster" {
  cluster_identifier   = "aurora-cluster"
  engine               = "aurora-postgresql"
  engine_version       = "15"
  database_name        = "appdb"
  master_username      = "appuser"
  master_password      = "apppassword"
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  db_subnet_group_name = aws_db_subnet_group.db_subnet_group.name
}

resource "aws_rds_cluster_instance" "aurora_instance" {
  identifier           = "aurora-instance"
  cluster_identifier   = aws_rds_cluster.aurora_cluster.id
  instance_class       = "db.t3.medium"
  engine               = "aurora-postgresql"
  engine_version       = "15"
  publicly_accessible  = false
  db_subnet_group_name = aws_db_subnet_group.db_subnet_group.name
}

resource "aws_security_group" "db_sg" {
  name        = "db-sg"
  vpc_id      = var.db_vpc_id
  description = "DB security group"
}

resource "aws_db_subnet_group" "db_subnet_group" {
  name       = "db-subnet-group"
  subnet_ids = var.db_private_subnet_ids
}
