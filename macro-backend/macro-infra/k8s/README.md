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

## Status of these manifests

**The manifests have not caught up with the services, and nothing here deploys the application today.** `java/deployment.yaml` and `java/service.yaml` still describe the retired monolith: they name an image, `macro-analyzer:latest`, and a database, `macroanalyzer`, that no module builds or uses any more. With no such image imported, the pod stays in `ErrImageNeverPull`. The four services (`regional-statistics-service`, `macro-series-service`, `parliament-service`, `election-service`) have no manifests yet: that is step 6 of `docs/microservice-migration-guide.md`, which also lists what is wrong with the other files here. Until then, run the application with Docker Compose (see `CLAUDE.md`).

## Database URL

The applications' defaults are the Compose Postgres, `localhost:5432`, which doesn't exist inside a pod, so a manifest has to set `SPRING_DATASOURCE_URL` to the cluster's Postgres, `192.168.154.7:31274`. 31274 is the Postgres service's NodePort. `postgres/postgres-service.yaml` doesn't pin it, so Kubernetes picked it, and it can change if the service is re-created.

With microk8s running, the NodePort also answers on `localhost:31274`. To use the cluster's database from outside, pass the URL deliberately, with `SPRING_DATASOURCE_URL`. No default points there.

## Sources
 - [Run postgres on kubernetes](https://phoenixnap.com/kb/postgresql-kubernetes)
 - [Run kafka on kubernetes](https://phoenixnap.com/kb/kafka-on-kubernetes)
 - [Install microk8s on raspberry pis](https://ubuntu.com/tutorials/how-to-kubernetes-cluster-on-raspberry-pi#1-overview)
 - [Microk8s ingress](https://phoenixnap.com/kb/microk8s-ingress)

## Copied from higher readme

### Apply postgres pods

```
k apply -f postgres/
```

### Port forward db from microk8s to local

Use a local port other than 5432. The applications default to `localhost:5432`, so forwarding the cluster's database to that port would connect them to it.

```
k port-forward <pod-name> 15432:5432
```