resource "terraform_data" "jenkins_instance" {
  provisioner "local-exec" {
    command     = "./deploy-jenkins.sh"
    working_dir = path.module
  }
}
