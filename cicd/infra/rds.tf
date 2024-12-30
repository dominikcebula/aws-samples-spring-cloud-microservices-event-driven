module "rds" {
  source = "./rds"

  db_vpc_id             = module.eks.vpc_id
  db_private_subnet_ids = module.eks.private_subnet_ids
}
