# k8s & istio workshop


## Preparing the environment

````console
$ minikube start --memory=8192 --cpus=4 --kubernetes-version=v1.13.1 \
    --extra-config=controller-manager.cluster-signing-cert-file="/var/lib/minikube/certs/ca.crt" \
    --extra-config=controller-manager.cluster-signing-key-file="/var/lib/minikube/certs/ca.key" \
    --vm-driver=virtualbox

$ cd deploy-artifacts/istio-1.1.7
$ export PATH=$PWD/bin:$PATH

$ for i in install/kubernetes/helm/istio-init/files/crd*yaml; do kubectl apply -f $i; done

$ kubectl apply -f install/kubernetes/istio-demo-auth.yaml
````

### Verify Istio

````console
$ kubectl get svc -n istio-system
````

## Install Helm

````console
$ curl -LO https://git.io/get_helm.sh
$ chmod 700 get_helm.sh
$ ./get_helm.sh

$ helm init --history-max 200

$ helm repo update
````

## Some more prep

### create demo namespaces and enable sidecar
````console
$ cd ..

$ kubectl apply -f demo-namespaces.yml

$ kubectl label namespace development istio-injection=enabled
````

### Install first common usage services (dev-commons)

````console
$ helm repo add incubator https://storage.googleapis.com/kubernetes-charts-incubator
$ helm install --namespace dev-commons --name kafka --set zookeeper.storage=1Gi --set persistence.size=1Gi incubator/kafka

$ helm install --namespace dev-commons --name redis --set persistentVolume.size=2Gi --set hardAntiAffinity=false stable/redis-ha

$ helm install --namespace dev-commons --name rabbitmq --set persistentVolume.size=2Gi stable/rabbitmq-ha
````


## kubernetes port forwarding

````console
$ kubectl get svc istio-ingressgateway -n istio-system -o yaml

$ kubectl get pod -n istio-system

(ingress)
$ kubectl port-forward $(kubectl get pod -n istio-system -l app=istio-ingressgateway \
    -o jsonpath='{.items[0].metadata.name}') -n istio-system 15000

(grafana)
$ kubectl port-forward $(kubectl get pod -n istio-system -l app=grafana \
    -o jsonpath='{.items[0].metadata.name}') -n istio-system 3000

(kiali)
$ kubectl port-forward $(kubectl get pod -n istio-system -l app=kiali \
    -o jsonpath='{.items[0].metadata.name}') -n istio-system 20001

(jaeger-query)
$ kubectl port-forward $(kubectl get pod -n istio-system -l app=jaeger \
    -o jsonpath='{.items[0].metadata.name}') -n istio-system 16686

````

## Deploy first service

Build new service
````console
$ cd request-api-project

$ mvn package
````
Push to docker registry
````console
$ docker images | grep sync
$ docker tag <hash> fsanti68/sync-service:latest
$ docker push fsanti68/sync-service:1.0
````

Deploy to kubernetes
````console
$ cd deploy

$ kubectl apply -f sync-api-deploy.yml

$ kubectl get pods -n development

$ EXTERNAL_IP=$(kubectl get svc -n istio-system -l app=istio-ingressgateway \
   	-o jsonpath='{.items[0].status.loadBalancer.ingress[0].ip}')
````

## Test the first service

````console
$ http http://$EXTERNAL_IP/sync/echo/Supercallifragilisticexpiralidocious
````

(correct deployment)
````console
$ kubectl apply -f http-gateway.yml

$ kubectl apply -f development-virtualservices.yml

$ http http://$EXTERNAL_IP/sync/echo/Supercallifragilisticexpiralidocious
````

Add a ConfigMap for /sync service:

(uncomment 'template' line)
````console
$ kubectl delete -f sync-api-deploy.yml

$ mvn package

$ docker push fsanti68/sync-service

$ kubectl apply -f sync-api-deploy-with-configmap.yml
````

Accessing Redis from inside pod
````console
$ kubectl exec -n dev-commons redis-redis-ha-server-0 -c redis -it sh
````

Forwarding redis port
````console
$ kubectl port-forward -n dev-commons svc/redis-redis-ha 6379:6379
````

Monitoring

(connect to Grafana)
````console
$ kubectl port-forward $(kubectl get pod -n istio-system -l app=grafana \
    -o jsonpath='{.items[0].metadata.name}') -n istio-system 3000
````

````console
$ kubectl apply -f grafana-vs.yml
$ kubectl apply -f prometheus-vs.yml
$ kubectl apply -f kiali-vs.yml
$ kubectl apply -f tracing-vs.yml
$ EXTERNAL_IP=$(kubectl get svc -n istio-system -l app=istio-ingressgateway \
   	-o jsonpath='{.items[0].status.loadBalancer.ingress[0].ip}')
````

Add \"$EXTERNAL_IP  k8scluster\" to /etc/hosts

