module "eks" {
  source = "./eks"

  aws_account_id = local.aws_account_id
  eks_region     = var.region
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
  cluster_name   = var.cluster_name
  my_public_ip   = data.http.my_public_ip.response_body

  depends_on = [module.vpc]
}
