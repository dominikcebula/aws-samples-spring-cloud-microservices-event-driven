data "http" "jenkins_operator_raw" {
  url = "https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/all-in-one-v1alpha2.yaml"
}

resource "kubectl_manifest" "jenkins_operator" {
  yaml_body = data.http.jenkins_operator_raw.response_body

  depends_on = [kubectl_manifest.jenkins_crd]
}
