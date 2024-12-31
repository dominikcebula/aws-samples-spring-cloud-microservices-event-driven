variable "aws_account_id" {
  description = "AWS Account ID"
  type        = string
}

variable "region" {
  description = "AWS region"
  type        = string
}

variable "db_vpc_id" {
  description = "DB VPC ID"
  type        = string
}

variable "db_subnet_ids" {
  description = "DB Subnet IDs"
  type = list(string)
}

variable "db_allow_access_from_subnets_cidr_blocks" {
  description = "List of subnet CIDR blocks which should be allowed to access db"
  type = list(string)
}

variable "role_app_runner_name" {
  description = "Name of the IAM role for Microservice App Runner"
  type        = string
}

variable "my_public_ip" {
  description = "Public IP Address to be added to public inbound traffic allow list"
  type        = string
}
