data "kubernetes_secret" "jenkins_user" {
  metadata {
    name = "jenkins-operator-credentials-master"
  }

  depends_on = [kubectl_manifest.jenkins_master]
}

output "jenkins_user" {
  value = data.kubernetes_secret.jenkins_user
}
