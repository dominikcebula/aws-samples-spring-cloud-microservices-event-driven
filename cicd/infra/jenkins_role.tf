module "jenkins_role" {
  source = "./jenkins_role"

  eks_cluster_name  = var.cluster_name
  eks_oidc_provider = module.eks.oidc_provider

  depends_on = [module.eks]
}
