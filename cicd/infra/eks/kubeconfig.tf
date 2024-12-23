resource "null_resource" "kubectl" {
  provisioner "local-exec" {
    command = "aws eks --region ${var.eks_region} update-kubeconfig --name ${var.cluster_name}"
  }

  depends_on = [module.eks]
}
