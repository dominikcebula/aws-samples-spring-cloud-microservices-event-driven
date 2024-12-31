module "rds" {
  source = "./rds"

  aws_account_id       = local.aws_account_id
  region               = var.region
  db_vpc_id             = module.eks.vpc_id
  db_private_subnet_ids = module.eks.private_subnet_ids
  role_app_runner_name = module.eks.role_app_runner_name
  my_public_ip         = data.http.my_public_ip.response_body
}
