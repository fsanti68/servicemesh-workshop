#!/bin/bash

set -e

CWD=$PWD
cd deploy-artifacts

if [ -x "$(command -v helm )" ]; then
   echo "Helm already installed: do nothing"
else
   echo "Installing Helm"
   curl -LO https://git.io/get_helm.sh
   chmod 700 get_helm.sh
   ./get_helm.sh

   helm init --history-max 200

   helm repo update
fi

cd $CWD
