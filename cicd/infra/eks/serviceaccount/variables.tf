variable "service_account_name" {
  description = "The name of service account to create"
}

variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "eks_cluster_name" {
  description = "Name of EKS Cluster to create"
  type        = string
}

variable "eks_region" {
  description = "AWS EKS region"
  type        = string
}

variable "eks_oidc_provider" {
  description = "The OpenID Connect identity provider (issuer URL without leading `https://`)"
}
