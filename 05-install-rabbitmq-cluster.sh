#!/bin/bash

set -e

if [[ "$(helm ls rabbitmq)" == *"rabbitmq"* ]]; then
   echo "rabbitmq cluster already installed:"
   helm ls
else
   echo "Install rabbitmq cluster"
   helm install --namespace dev-commons --name rabbitmq --set persistentVolume.size=2Gi stable/rabbitmq-ha
fi
