#!/bin/bash

function deploy {
  NAME=$1
  URL=$2

  echo "===> Deploying ${NAME}"

  kubectl apply -f "${URL}" || {
    echo "===> Error while installing ${NAME}";
    exit 1;
  }

  echo "===> Finished deployment of ${NAME}"
  echo
}

deploy "Jenkins Custom Resource Definition" \
  https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml

deploy "Jenkins Operator and other required resources:" \
  https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/all-in-one-v1alpha2.yaml

MY_PUBLIC_IP=$(curl https://ifconfig.me/ip 2> /dev/null)
export MY_PUBLIC_IP

JENKINS_GITHUB_TOKEN_USERNAME=$(aws secretsmanager get-secret-value --secret-id jenkins-github-token --query SecretString --output text |jq --raw-output '.github_username')
export JENKINS_GITHUB_TOKEN_USERNAME

JENKINS_GITHUB_TOKEN_PASSWORD=$(aws secretsmanager get-secret-value --secret-id jenkins-github-token --query SecretString --output text |jq --raw-output '.github_password')
export JENKINS_GITHUB_TOKEN_PASSWORD

envsubst < jenkins_instance.yaml.tpl > jenkins_instance.yaml
deploy "Jenkins Instance" jenkins_instance.yaml

echo -n "Jenkins User = "
kubectl get secret jenkins-operator-credentials-master -o 'jsonpath={.data.user}' | base64 -d
echo
echo -n "Jenkins Password = "
kubectl get secret jenkins-operator-credentials-master -o 'jsonpath={.data.password}' | base64 -d
echo
