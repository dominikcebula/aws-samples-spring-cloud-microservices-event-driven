variable "eks_region" {
  description = "AWS EKS region"
  type        = string
}

variable "my_public_ip" {
  description = "Public IP Address to be added to public inbound traffic allow list"
  type        = string
}
