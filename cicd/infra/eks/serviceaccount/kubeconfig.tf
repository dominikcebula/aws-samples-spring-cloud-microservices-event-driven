resource "null_resource" "kubectl" {
  provisioner "local-exec" {
    command = "aws eks --region ${var.eks_region} update-kubeconfig --name ${var.eks_cluster_name}"
  }
}
