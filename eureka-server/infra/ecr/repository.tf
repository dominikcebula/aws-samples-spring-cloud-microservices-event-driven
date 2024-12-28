resource "aws_ecr_repository" "eureka_server" {
  name         = "aws-samples-spring-cloud-microservices-event-driven/eureka-server"
  force_delete = true
}
