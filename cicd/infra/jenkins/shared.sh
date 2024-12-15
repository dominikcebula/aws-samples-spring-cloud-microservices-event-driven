#!/bin/bash

function deploy {
  NAME=$1
  URL=$2

  kubectl apply -f "${URL}" || {
    echo "Error while installing ${NAME}";
    exit 1;
  }
}
