#!/bin/bash

. shared.sh

deploy "Jenkins Custom Resource Definition" \
  https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/config/crd/bases/jenkins.io_jenkins.yaml

deploy "Jenkins Operator and other required resources:" \
  https://raw.githubusercontent.com/jenkinsci/kubernetes-operator/master/deploy/all-in-one-v1alpha2.yaml
