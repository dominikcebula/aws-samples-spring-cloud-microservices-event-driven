module "eks" {
  source = "./eks"

  eks_region = var.region
}
