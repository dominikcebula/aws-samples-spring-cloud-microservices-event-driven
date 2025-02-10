module "codeartifact_repository" {
  source = "./codeartifact_repository"

  jenkins_cicd_role_arn = module.jenkins.jenkins_cicd_role_arn
}
