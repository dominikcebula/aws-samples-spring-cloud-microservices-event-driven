variable "eks_cluster_name" {
  description = "Name of EKS Cluster to create"
  type        = string
}

variable "eks_oidc_provider" {
  description = "The OpenID Connect identity provider (issuer URL without leading `https://`)"
  type        = string
}
