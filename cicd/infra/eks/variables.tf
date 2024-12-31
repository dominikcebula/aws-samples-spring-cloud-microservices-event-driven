variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "eks_region" {
  description = "AWS EKS region"
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC where the cluster will be provisioned"
  type        = string
}

variable "subnet_ids" {
  description = "A list of subnet IDs where the nodes/node groups will be provisioned."
  type = list(string)
}

variable "my_public_ip" {
  description = "Public IP Address to be added to public inbound traffic allow list"
  type        = string
}

variable "cluster_name" {
  description = "Name of EKS Cluster to create"
  type        = string
}
