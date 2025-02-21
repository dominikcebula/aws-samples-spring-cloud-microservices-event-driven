variable "customer_events_topic_arn" {
  description = "Customer Events Topic ARN"
}

variable "service_account_id" {
  description = "Service Account ID used to run Microservice"
  type        = string
}

variable "service_account_arn" {
  description = "Service Account ARN used to run Microservice"
  type        = string
}
