module "rds" {
  source = "./rds"

  aws_account_id                           = local.aws_account_id
  region                                   = var.region
  db_vpc_id                                = module.vpc.vpc_id
  db_subnet_ids                            = module.vpc.public_subnets
  db_allow_access_from_subnets_cidr_blocks = module.vpc.private_subnets_cidr_blocks
  service_accounts_roles = module.eks.service_accounts_roles
  my_public_ip                             = data.http.my_public_ip.response_body
}
