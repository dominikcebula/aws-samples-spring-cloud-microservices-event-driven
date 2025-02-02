module "service_accounts" {
  source = "./serviceaccount"

  for_each = toset(["microservice-customers", "microservice-shipment"])

  service_account_name = each.key

  aws_account_id    = var.aws_account_id
  eks_cluster_name  = module.eks.cluster_name
  eks_region        = var.eks_region
  eks_oidc_provider = module.eks.oidc_provider

  depends_on = [module.eks]
}
