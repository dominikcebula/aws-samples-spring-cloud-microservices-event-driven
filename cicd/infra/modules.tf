module "eureka_server" {
  source = "./../../eureka-server/infra"
}

module "config_server" {
  source = "./../../config-server/infra"
}

module "spring_boot_admin" {
  source = "./../../spring-boot-admin/infra"
}

module "spring_cloud_gateway" {
  source = "./../../spring-cloud-gateway/infra"
}

module "microservice_customers" {
  source = "./../../microservice-customers/infra"

  service_account_id = module.eks.service_accounts_id_map["microservice-customers"]
  service_account_arn = module.eks.service_accounts_arn_map["microservice-customers"]
}

module "microservice_shipment" {
  source = "./../../microservice-shipment/infra"

  customer_events_topic_arn = module.microservice_customers.customer_events_topic_arn
  service_account_id  = module.eks.service_accounts_id_map["microservice-shipment"]
  service_account_arn = module.eks.service_accounts_arn_map["microservice-shipment"]
}
