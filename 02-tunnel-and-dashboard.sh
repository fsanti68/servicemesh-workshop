#!/bin/bash

set -e

echo "Minikube serving at $(minikube ip)"

CWD=$PWD
cd deploy-artifacts
if [[ -d istio-1.1.7 ]]; then
   echo "Istio 1.1.7 already installed: do nothing"
else
   echo "Installing Istio 1.1.7"
   curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.1.7 sh -
   for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done

   kubectl apply -f install/kubernetes/istio-demo-auth.yaml

   kubectl get svc -n istio-system
fi

cd $CWD

echo "Setting minikube tunnel"
minikube tunnel --log_dir /tmp &

echo "Setting minikube dashboard"
minikube dashboard --log_dir /tmp &
