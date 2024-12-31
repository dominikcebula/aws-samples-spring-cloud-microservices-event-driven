module "eks" {
  source = "./eks"

  aws_account_id = local.aws_account_id
  eks_region     = var.region
  cluster_name   = var.cluster_name
  my_public_ip   = data.http.my_public_ip.response_body
}
