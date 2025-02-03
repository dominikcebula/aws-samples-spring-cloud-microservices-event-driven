output "cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "Security group ids attached to the cluster control plane"
  value       = module.eks.cluster_security_group_id
}

output "region" {
  description = "AWS region"
  value       = var.eks_region
}

output "cluster_name" {
  description = "Kubernetes Cluster Name"
  value       = module.eks.cluster_name
}

output "oidc_provider" {
  value = module.eks.oidc_provider
}

output "service_accounts_roles" {
  description = "Name of the IAM roles for Microservice Runners Service Accounts"
  value = toset(module.service_accounts[*].service_account_role_name)
}

output "service_accounts_arn_map" {
  description = "Name of the IAM roles for Microservice Runners Service Accounts"
  value = zipmap(module.service_accounts[*].service_account_role_name, module.service_accounts[*].service_account_role_arn)
}
