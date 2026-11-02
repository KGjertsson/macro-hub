# 0006. Kubernetes as the eventual deployment target

## Status

Accepted

## Date

2026-11-01

## Context

The app isn't deployed anywhere today, so there is no production data, downtime or live traffic to protect during the split.

Local development uses Docker Compose. `docker/docker-compose-all.yml` runs the full stack, including the frontend's nginx image. `k8s/` holds the manifests, for Postgres and the backend, with no frontend manifest. They need work before a real deployment:

- `java/service.yaml` selects `app: macro-analyzer`, but the pods in `java/deployment.yaml` are labelled `app: macro-analyzer-java-app`.
- `ingress/test-ingress.yaml` routes `/` to a Service named `macro-analyzer-java-app`, which no manifest defines.
- The backend uses `imagePullPolicy: Never` with images imported by hand (`docker save | microk8s ctr image import -`).
- `postgres/postgres-deployment.yaml` uses `postgres:13`, while Compose uses `postgres:16`.

## Decision

Kubernetes is the target for deployment. Docker Compose stays the default route for local development, during and after the split, so `docker-compose-all.yml` runs all services at each stage.

The k8s files only need to keep working as far as they do today until the first deployment, which is its own step in the [migration guide](../microservice-migration-guide.md).

This ADR doesn't decide how routing works in the cluster. The choice between nginx in the frontend image and Ingress routes is an open question in the guide.

*Note, 2026-11-02:* the monolith this ADR's context describes is retired, but the manifests below still name it. Step 6 of the [migration guide](../microservice-migration-guide.md) replaces them. The service name `macro-analyzer-service` below is the example from the old manifests.

*Note, 2026-11-02:* step 6 made the first deployment, in its own namespace `macro-hub`. **The gateway in the cluster is nginx in the frontend image,** which the project owner decided, on a pinned NodePort (30081) with fully qualified service names. There are no Ingress routes and the `ingress` addon stays off: nginx already holds the routes, the 404s and the 30-minute timeout, and Compose and the cluster then behave the same. Images stay on `imagePullPolicy: Never`, tagged with the commit and imported by `k8s/build-images.sh`, with no registry. The other consequences below are done, and `k8s/README.md` is the procedure. The Context's list describes files that are gone.

## Alternatives considered

- **Compose as the deployment target as well.** It already runs the whole stack, but the project owner wants to deploy on Kubernetes.

## Consequences

- `k8s/` needs a frontend manifest or Ingress routes to act as the gateway, whichever the open question settles on.
- If nginx in the frontend image is the gateway, it needs fully qualified service names such as `http://macro-analyzer-service.default.svc.cluster.local:8080`, because its resolver ignores `resolv.conf` search domains.
- The first deployment is its own step in the guide, which needs manifests for all four services, routing, Postgres databases and roles, and a way to deliver four images.
- Until then, each step only has to maintain Compose. The manifests fall behind the services, and the first deployment catches them up.
- Compose and the cluster stay two sets of deploy files in the long term, one for local development and one for deployment.
