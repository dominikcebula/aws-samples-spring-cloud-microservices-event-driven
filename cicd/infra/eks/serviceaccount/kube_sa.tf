resource "terraform_data" "kube_sa" {
  provisioner "local-exec" {
    command = "envsubst < kube_sa_template.tpl | kubectl apply -f -"
    working_dir = path.module

    environment = {
      AWS_ACCOUNT_ID       = var.aws_account_id
      SERVICE_ACCOUNT_NAME = local.service_account_role_name
    }
  }

  depends_on = [null_resource.kubectl]
}
