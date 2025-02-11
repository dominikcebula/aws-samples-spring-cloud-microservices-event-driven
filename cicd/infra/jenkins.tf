module "jenkins" {
  source = "./jenkins"

  eks_cluster_name  = var.cluster_name
  eks_oidc_provider = module.eks.oidc_provider

  depends_on = [module.eks, module.codeartifact_repository]
}
