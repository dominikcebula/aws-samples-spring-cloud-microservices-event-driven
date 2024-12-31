variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "eks_region" {
  description = "AWS EKS region"
  type        = string
}

variable "my_public_ip" {
  description = "Public IP Address to be added to public inbound traffic allow list"
  type        = string
}

variable "cluster_name" {
  description = "Name of EKS Cluster to create"
  type        = string
}
