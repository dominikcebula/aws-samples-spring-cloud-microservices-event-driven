variable "db_vpc_id" {
  description = "DB VPC ID"
  type        = string
}

variable "db_private_subnet_ids" {
  description = "DB Private Subnet IDs"
  type = list(string)
}
