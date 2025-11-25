## Ingress
```
1. microk8s enable ingress // enable ingress
2. microk8s kubectl -n ingress get configmap // list config map
3. microk8s kubectl -n ingress edit configmaps nginx-load-balancer-microk8s-conf // edit config map
4. Add
data:
  use-forwarded-headers: "true"
5. 
```

## Configure pods on cluster using repo clone
1. create namespace macro-analyzer: `k create namespace macro-analyzer`
2. set the new namespace as active: `microk8s kubectl config set-context --current --namespace=macro-analyzer`
3. run postgres container: ` k apply -f postgres/`

## Java

Build new image and make available to microk8s:

```
mvn install
sudo docker build -t macro-analyzer:latest .
sudo docker save macro-analyzer:latest | microk8s ctr image import -
```

## Sources
 - [Run postgres on kubernetes](https://phoenixnap.com/kb/postgresql-kubernetes)
 - [Run kafka on kubernetes](https://phoenixnap.com/kb/kafka-on-kubernetes)
 - [Install microk8s on raspberry pis](https://ubuntu.com/tutorials/how-to-kubernetes-cluster-on-raspberry-pi#1-overview)
 - [Microk8s ingress](https://phoenixnap.com/kb/microk8s-ingress)

## Copied from higher readme


### Build docker image

```
sudo docker build -t macro-analyzer:latest .
```

### Run docker image

```
docker run -e "SPRING_PROFILES_ACTIVE=prod -p 8080:8080 macro-analyzer
```

### Upload build docker image to local microk8s storage

```
sudo docker save macro-analyzer:latest | microk8s ctr image import -
```

### Apply postgres pods

```
k apply -f postgres/
```

### Port forward db from microk8s to local

```
k port-forward <pod-name> 5432:5432
```