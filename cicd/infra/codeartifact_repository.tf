module "codeartifact_repository" {
  source = "./codeartifact_repository"

  jenkins_cicd_role_arn = module.jenkins_role.jenkins_cicd_role_arn

  depends_on = [module.jenkins_role]
}
