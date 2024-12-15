#!/bin/bash

. shared.sh

deploy "Jenkins Instance" jenkins_instance.yaml

#kubectl get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.user}' | base64 -d
#kubectl get secret jenkins-operator-credentials-jenkins -o 'jsonpath={.data.password}' | base64 -d
