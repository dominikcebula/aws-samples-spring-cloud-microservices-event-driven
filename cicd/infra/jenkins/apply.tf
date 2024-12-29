resource "terraform_data" "jenkins_instance" {
  provisioner "local-exec" {
    command     = "./deploy-jenkins.sh"
    working_dir = path.module
  }

  depends_on = [aws_iam_role.jenkins_cicd_role]
}
