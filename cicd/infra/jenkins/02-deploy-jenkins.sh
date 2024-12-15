#!/bin/bash

. shared.sh

MY_PUBLIC_IP=$(curl https://ifconfig.me/ip 2> /dev/null)
export MY_PUBLIC_IP

envsubst < jenkins_instance.yaml.tpl > jenkins_instance.yaml
deploy "Jenkins Instance" jenkins_instance.yaml

#kubectl get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.user}' | base64 -d
#kubectl get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.password}' | base64 -d
