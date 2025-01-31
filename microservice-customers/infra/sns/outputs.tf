output "customer_events_topic_arn" {
  description = "Customer Events Topic ARN"
  value       = aws_sns_topic.customer_events_topic.arn
}
