module "eks" {
  source = "./eks"

  eks_region   = var.region
  cluster_name = var.cluster_name
  my_public_ip = data.http.my_public_ip.response_body
}
