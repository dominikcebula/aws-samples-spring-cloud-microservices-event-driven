resource "terraform_data" "jenkins_instance" {
  provisioner "local-exec" {
    command     = "envsubst < sa_app_runner.yaml.tpl | kubectl apply -f -"
    working_dir = path.module

    environment = {
      AWS_ACCOUNT_ID = var.aws_account_id
    }
  }

  depends_on = [null_resource.kubectl]
}
