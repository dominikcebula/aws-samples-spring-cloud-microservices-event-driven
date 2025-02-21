output "service_account_name" {
  description = "Name of the Service Account created"
  value       = local.service_account_sa_name
}

output "service_account_role_id" {
  description = "Name of the IAM role for Service Account"
  value       = aws_iam_role.service_account_role.id
}

output "service_account_role_name" {
  description = "Name of the IAM role for Service Account"
  value       = aws_iam_role.service_account_role.name
}

output "service_account_role_arn" {
  description = "ARN of the IAM role for Service Account"
  value       = aws_iam_role.service_account_role.arn
}
