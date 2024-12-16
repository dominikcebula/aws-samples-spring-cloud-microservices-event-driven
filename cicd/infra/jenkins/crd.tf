data "http" "jenkins_crd_raw" {
  url = "https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml"
}

resource "kubectl_manifest" "jenkins_crd" {
  yaml_body = data.http.jenkins_crd_raw.response_body
}
