module "ecr" {
  source = "./ecr"
}

module "sqs" {
  source = "./sqs"

  customer_events_topic_arn = var.customer_events_topic_arn
  service_account_arn = var.service_account_arn
}
