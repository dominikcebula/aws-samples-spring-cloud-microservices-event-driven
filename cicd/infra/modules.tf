module "eureka_server" {
  source = "./../../eureka-server/infra"
}

module "config_server" {
  source = "./../../config-server/infra"
}
