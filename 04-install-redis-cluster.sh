#!/bin/bash

set -e

if [[ "$(helm ls redis)" == *"redis"* ]]; then
   echo "redis cluster already installed:"
   helm ls
else
   echo "Install redis cluster"
   helm install --namespace dev-commons --name redis --set persistentVolume.size=2Gi stable/redis-ha
fi
