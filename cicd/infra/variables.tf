variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-central-1"
}

variable "cluster_name" {
  description = "Name of EKS Cluster onto which Jenkins should be deployed"
  type        = string
  default     = "eks-01"
}
