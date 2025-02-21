module "ecr" {
  source = "./ecr"
}

module "role" {
  source = "./role"

  service_account_id        = var.service_account_id
  customer_events_topic_arn = module.sns.customer_events_topic_arn
}

module "sns" {
  source = "./sns"

  service_account_arn = var.service_account_arn
}
