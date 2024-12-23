module "jenkins" {
  source = "./jenkins"

  depends_on = [module.eks]
}
