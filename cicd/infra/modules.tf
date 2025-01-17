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
}

module "microservice_shipment" {
  source = "./../../microservice-shipment/infra"
}
