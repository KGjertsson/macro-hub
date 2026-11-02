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

## Database URL

The application's defaults are the Compose Postgres, `localhost:5432`, which doesn't exist inside a pod. So `java/deployment.yaml` sets `SPRING_DATASOURCE_URL` to the cluster's Postgres, `192.168.154.7:31274`. 31274 is the Postgres service's NodePort. `postgres/postgres-service.yaml` doesn't pin it, so Kubernetes picked it, and it can change if the service is re-created: then update the manifest. Run `k apply -f java/` before deploying the next image: on a Deployment without this setting, the pod falls back to `localhost:5432` and fails to start.

With microk8s running, the NodePort also answers on `localhost:31274`. To use the cluster's database from outside, pass the URL deliberately, for example `SPRING_DATASOURCE_URL=jdbc:postgresql://192.168.154.7:31274/macroanalyzer`.

## Java

`java/deployment.yaml` only runs the monolith, which is what's left there of the API: the services that have moved (`regional-statistics-service`, `macro-series-service`, `parliament-service`) have no manifests until step 6 of `docs/microservice-migration-guide.md`. So in the cluster `/parliament/*` answers 404, and election uses Valmyndigheten's party colours, as it has no `ELECTION_PARLIAMENT_URL` to reach.

Build new image and make available to microk8s:

From `macro-backend/`:

```
mvn clean package
sudo docker build -t macro-analyzer:latest macro-analyzer
sudo docker save macro-analyzer:latest | microk8s ctr image import -
```

## Sources
 - [Run postgres on kubernetes](https://phoenixnap.com/kb/postgresql-kubernetes)
 - [Run kafka on kubernetes](https://phoenixnap.com/kb/kafka-on-kubernetes)
 - [Install microk8s on raspberry pis](https://ubuntu.com/tutorials/how-to-kubernetes-cluster-on-raspberry-pi#1-overview)
 - [Microk8s ingress](https://phoenixnap.com/kb/microk8s-ingress)

## Copied from higher readme


### Build docker image

From `macro-backend/`, after `mvn clean package`:

```
sudo docker build -t macro-analyzer:latest macro-analyzer
```

### Run docker image

```
docker run -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/macroanalyzer -p 8080:8080 macro-analyzer
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

Use a local port other than 5432. The applications default to `localhost:5432`, so forwarding the cluster's database to that port would connect them to it.

```
k port-forward <pod-name> 15432:5432
```