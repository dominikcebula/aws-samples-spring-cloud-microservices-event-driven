module "jenkins" {
  source = "./jenkins"

  my_public_ip = data.http.my_public_ip.response_body

  depends_on = [module.eks]
}
