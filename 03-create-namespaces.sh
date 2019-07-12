#!/bin/bash

set -e

echo "Create namespaces (dev-commons and development)"

lines=$(kubectl get namespaces | egrep "dev-commons|development" | wc -l)
if [[ $lines -lt 2 ]]; then
   kubectl apply -f ./deploy-artifacts/dev-common-namespace.yml
fi

