#!/bin/bash

minikube start --memory=8192 --cpus=4 --kubernetes-version=v1.13.1 \
    --extra-config=controller-manager.cluster-signing-cert-file="/var/lib/minikube/certs/ca.crt" \
    --extra-config=controller-manager.cluster-signing-key-file="/var/lib/minikube/certs/ca.key" \
    --vm-driver=virtualbox
