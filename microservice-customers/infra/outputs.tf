output "customer_events_topic_arn" {
  description = "Customer Events Topic ARN"
  value       = module.sns.customer_events_topic_arn
}
