#!/bin/bash

set -e

if [[ ! "$(helm repo list)" == *"incubator"* ]]; then
   echo "Add 'incubator' repository for Helm"
   helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
fi

if [[ "$(helm ls kafka)" == *"kafka"* ]]; then
   echo "Kafka cluster already installed:"
   helm ls
else
   echo "Install Kafka + Zookeeper clusters"
   helm install --namespace dev-commons --name kafka --set zookeeper.storage=1Gi --set persistence.size=1Gi incubator/kafka
fi