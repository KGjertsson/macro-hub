# Deploying macro-hub on microk8s

The whole app in one namespace, `macro-hub`, on a single-node microk8s: Postgres 16, the four services, and the frontend, whose nginx is the gateway ([ADR 0006](../../../docs/adrs/0006-kubernetes-as-eventual-deployment-target.md)). Local development stays on Docker Compose (`CLAUDE.md`).

**What these files don't manage:** the older namespace `macro-analyzer`, with its Postgres 13, its volume `postgres-pv` on `/mnt/data/postgres` and its NodePort 31274, and the namespace `gjertchenbot`. Every command here stays inside `macro-hub`, apart from the two PersistentVolumes named `macro-hub-*`.

| What | Where |
| --- | --- |
| The app | `http://localhost:30081` on this machine, also on the WSL2 address (`hostname -I`). A NodePort, pinned in `frontend/frontend.yaml` |
| Postgres | `postgres.macro-hub.svc.cluster.local:5432`. ClusterIP only, no NodePort. This machine's own routes do reach a ClusterIP (kube-proxy), also from its Docker containers, and every role needs its password there |
| Data and dumps | `/mnt/data/macro-hub/postgres`, `/mnt/data/macro-hub/backups` (retained volumes) |
| Database passwords | `~/.config/macro-hub/k8s-db-passwords.env`, outside the repo, generated once. Keep it |

## Before the first deployment

- microk8s with `dns`, and your user in the `microk8s` group (`kubernetes_setup.sh` installs this). No other addon is needed: there is no Ingress, and the volumes are static, so they need no storage addon.
- JDK 25 (`~/.sdkman/candidates/java/current`, or set `JAVA_HOME`), Docker and `openssl`.
- The API keys in `macro-backend/macro-series-service/.env` (copy its `.env.example`).

## Bringing it up

From the repo root of the clone, on the commit you want to deploy:

```bash
cd macro-backend/macro-infra/k8s

# 1. Build the images from the current commit and import them into microk8s. About 3 minutes
./build-images.sh

# 2. Create the namespace and the Secrets. Prints no values. In a git worktree, or with the keys elsewhere, pass the env file
./create-secrets.sh

# 3. Apply everything, in order, and wait for it. About 2 minutes
./deploy.sh
```

Then look at it:

```bash
microk8s kubectl -n macro-hub get pods,svc,pvc,cronjob
```

A new database has its configs but no data. **Fill it once**, through the gateway, from the repo root. It calls the real external APIs with your keys:

```bash
macro-backend/macro-infra/scripts/scrape-all.sh http://localhost:30081
```

It took 8 minutes on the first run. Then check that no pod restarted (`RESTARTS` in `get pods`). The metrics addon is off, so read a pod's peak memory from its cgroup:

```bash
microk8s kubectl -n macro-hub exec deploy/regional-statistics-service -- cat /sys/fs/cgroup/memory.peak
```

**Every service's cron also scrapes in the 22:00 UTC hour**, so the cluster and a running Compose stack both go to the same sources every night, from the same machine, with the same keys. Don't start a deployment or a manual scrape between 21:55 and 23:00 UTC.

## Deploying a new version

Commit, then `./build-images.sh` and `./deploy.sh`. The images are tagged with the commit, so `kubectl -n macro-hub get deploy -o wide` shows what runs. Older tags stay in Docker and in containerd until you remove them by hand, from both:

```bash
docker rmi macro-hub/<name>:<tag>
/snap/microk8s/current/bin/ctr --address /var/snap/microk8s/common/run/containerd.sock -n k8s.io images rm docker.io/macro-hub/<name>:<tag>
```

## Taking it down

```bash
./teardown.sh
```

This removes what `deploy.sh` and `create-secrets.sh` made, by name. It keeps the namespace, both PersistentVolumes, their data and the passwords file, so `create-secrets.sh` and `deploy.sh` bring the same data back with no scrape. To delete the data as well, afterwards:

```bash
microk8s kubectl delete pv macro-hub-postgres-pv macro-hub-backups-pv
sudo rm -rf /mnt/data/macro-hub
rm ~/.config/macro-hub/k8s-db-passwords.env
```

## Backups

A CronJob, `postgres-backup`, runs at 03:00 UTC and dumps each database with `pg_dump` to `/mnt/data/macro-hub/backups/<database>/<database>-<time>.dump`. It keeps the newest 7 per database, and a failed dump removes nothing. To run one now, and see the result:

```bash
microk8s kubectl -n macro-hub create job --from=cronjob/postgres-backup backup-by-hand
microk8s kubectl -n macro-hub wait --for=condition=complete --timeout=600s job/backup-by-hand
microk8s kubectl -n macro-hub logs job/backup-by-hand
microk8s kubectl -n macro-hub delete job backup-by-hand
```

### Restoring a backup

The Postgres pod mounts the dumps read-only at `/backups`.

To look at a dump in a scratch database, without touching the service:

```bash
k() { microk8s kubectl -n macro-hub "$@"; }
k exec deploy/postgres -- ls -l /backups/election
k exec deploy/postgres -- sh -c 'createdb -U "$POSTGRES_USER" restore_check \
  && pg_restore -U "$POSTGRES_USER" -d restore_check --no-owner --exit-on-error /backups/election/<file>.dump'
k exec deploy/postgres -- sh -c 'psql -U "$POSTGRES_USER" -d restore_check -c "SELECT count(*) FROM local_election_area"'
k exec deploy/postgres -- sh -c 'dropdb -U "$POSTGRES_USER" restore_check'
```

To replace a service's database (here `election`) with a dump. Stop the service and wait for its pod to go, re-create the database with the init script, which also restores who may connect, and then restore as its owner. Run these one at a time, and stop if one fails:

```bash
k scale deployment/election-service --replicas=0
k wait --for=delete pod -l app=election-service --timeout=90s
k exec deploy/postgres -- sh -c 'dropdb -U "$POSTGRES_USER" election'
k exec -i deploy/postgres -- sh -s < ../docker/postgres-init/01-service-databases.sh
k exec deploy/postgres -- sh -c 'pg_restore -U "$POSTGRES_USER" -d election --exit-on-error /backups/election/<file>.dump'
k scale deployment/election-service --replicas=1
```

## Secrets

`create-secrets.sh` makes these, and no value is in the repo:

| Secret | Holds | Used by |
| --- | --- | --- |
| `postgres-bootstrap` | the bootstrap superuser's password and all four role passwords | Postgres (the init script), the backup job (only the superuser's) |
| `<service>-db` (`regional-statistics-db`, `macro-series-db`, `parliament-db`, `election-db`) | that service's own password | that service |
| `macro-series-api-keys` | the Riksbanken and Fed keys | `macro-series-service` only |

Check them with `kubectl -n macro-hub describe secret <name>`, which shows sizes and no values.

`deploy.sh` sets the roles' passwords on Postgres to the Secret's, every time. So to change passwords, or if the passwords file is lost, while keeping the data:

```bash
rm -f ~/.config/macro-hub/k8s-db-passwords.env   # if it still exists
./create-secrets.sh
# Pods read Secrets when they start, so Postgres first, to see the new values
microk8s kubectl -n macro-hub rollout restart deployment/postgres
./deploy.sh
microk8s kubectl -n macro-hub rollout restart deployment/regional-statistics-service deployment/macro-series-service deployment/parliament-service deployment/election-service
```

To change an API key: edit the env file, run `./create-secrets.sh`, then `microk8s kubectl -n macro-hub rollout restart deployment/macro-series-service`. `deploy.sh` doesn't restart anything for a changed Secret.

## After WSL2 or microk8s restarts

**This hasn't been tried.** Nothing here depends on a person being present: the images, volumes and Secrets persist, and the pods should come back by themselves. A service that starts before Postgres answers exits and is restarted with a backoff, so give it a few minutes. To check: `microk8s kubectl -n macro-hub get pods`, then open the URL. A CronJob run missed while the machine was off runs when it is back.

## Limits to know

- **The cluster has no RBAC (`--authorization-mode=AlwaysAllow`), so a namespace is not a security boundary here.** Any Kubernetes credential can do anything in any namespace, which is why no pod here mounts a service account token. Treat the `kubectl` access of the `microk8s` group as cluster-admin.
- **Backups share a disk with the data,** so they protect against a bad write or a bad migration, not a lost disk. Copy `/mnt/data/macro-hub/backups` elsewhere for that.

- **One replica each.** `scrapeLock` is per process, so a second instance would scrape the same queue at the same time. Service and Postgres Deployments use `strategy: Recreate`, so there is a short gap during a deploy.
- **The gateway has no authentication,** and anyone who can reach port 30081 can trigger scrapes ([ADR 0004](../../../docs/adrs/0004-keep-manual-scrape-endpoints-public-for-now.md)).
- **A query that hangs isn't noticed.** With no `socketTimeout`, a service whose database host vanishes without closing its connections keeps `scrapeLock` until it is restarted ("Database" in `CLAUDE.md`). On one node, Postgres goes down with the services, so this is unlikely. A liveness probe wouldn't help, because the process still answers.
- **A hostPath doesn't enforce the volume's size,** and images aren't protected from the kubelet's image garbage collection under disk pressure. If a pod says `ErrImageNeverPull`, run `./build-images.sh` again.
