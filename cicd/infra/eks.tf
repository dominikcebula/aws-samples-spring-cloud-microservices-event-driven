module "eks" {
  source = "./eks"

  eks_region   = var.region
  my_public_ip = data.http.my_public_ip.response_body
}
