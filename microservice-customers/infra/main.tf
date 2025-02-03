module "ecr" {
  source = "./ecr"
}

module "sns" {
  source = "./sns"

  service_account_arn = var.service_account_arn
}
