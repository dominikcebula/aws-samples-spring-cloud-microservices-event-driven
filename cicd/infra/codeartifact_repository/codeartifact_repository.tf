resource "aws_codeartifact_domain" "this" {
  domain = "spring-cloud-microservices-event-driven"
}

resource "aws_codeartifact_repository" "this" {
  repository = "spring-cloud-microservices-event-driven"
  domain     = aws_codeartifact_domain.this.domain
}
