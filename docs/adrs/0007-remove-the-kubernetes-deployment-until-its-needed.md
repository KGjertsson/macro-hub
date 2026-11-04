# 0007. Remove the Kubernetes deployment until it's needed

## Status

Accepted. It amends [0006](0006-kubernetes-as-eventual-deployment-target.md) without superseding it: Kubernetes may still be the eventual target.

## Date

2026-11-04

## Context

Step 6 of the [migration guide](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md#step-6-first-real-kubernetes-deployment) deployed the app to microk8s under WSL2 on 2026-11-02, from `macro-backend/macro-infra/k8s/`. It was a test cluster: the scrapes and the requests through its gateway were tested, and no page was ever seen rendered from it.

The project owner won't run Kubernetes on this machine, so nothing runs those files. CI doesn't use them, and neither does Compose, so a change to a service, its port or its migrations wouldn't show up as a broken manifest. They would fall behind unnoticed, as [0006](0006-kubernetes-as-eventual-deployment-target.md) already expected between deployments.

The project owner decided this.

## Decision

Delete `macro-backend/macro-infra/k8s/`, and create a deployment fresh when one is needed. Docker Compose is the only way to run the app until then.

Commit `584119f` (PR #91) has the last working version, for reference. The migration guide, finished with step 6, was removed in the same PR: the [link above](https://github.com/KGjertsson/macro-hub/blob/584119f/docs/microservice-migration-guide.md) is its last version, and the findings below are the part kept here.

## Alternatives considered

- **Keep the files, unused.** Nothing would notice them falling behind, and `CLAUDE.md` would go on describing a deployment that doesn't run.
- **Keep them on a branch or in another folder, marked as unmaintained.** Commit `584119f` already gives this, with nothing to look after.

## Consequences

- The project owner has uninstalled microk8s, so nothing on port 31274 or in the `macro-analyzer` namespace exists any more, and that port is no longer a hazard on this machine.
- The cluster's nightly `pg_dump` went with it, and Compose's databases have none, so nothing here makes backups any more. `parliament` under Compose is still the only copy of its data, as `CLAUDE.md` says.
- Nothing limits the services' memory, as Compose has always run them: no container limit, no `-Xmx`, and the JVM's own choice of collector.
- Application code is unchanged. `spring-boot-starter-actuator` stays in each service, though only the deployment's probes used its health endpoints.
- The manual scrape endpoints have to be looked at again when the app is next exposed ([0004](0004-keep-manual-scrape-endpoints-public-for-now.md)).

## Findings for a new deployment

**Reaching and running the cluster:**

- **`http://localhost:30081` didn't work from a Windows browser under WSL NAT networking.** The project owner opened it and saw nothing. `Test-NetConnection` from Windows then failed on `localhost:30081` and succeeded on `192.168.154.7:30081`, the WSL machine's own address. WSL only forwards `localhost` to a listening socket, and a NodePort has none, as kube-proxy's rules handle it. No page was seen rendered from the cluster at either address.
- **Run `microk8s enable rbac`.** The API server was `--authorization-mode=AlwaysAllow`, so any token could do anything in any namespace, and a namespace wasn't a security boundary. That is why no pod mounted a service account token.
- **Calico blocked pod deletion during teardown.** All pods stayed `Terminating`, with `FailedKillPod`: `failed to destroy network for sandbox ... plugin type="calico" failed (delete): error getting ClusterInformation: connection is unauthorized: Unauthorized`. That message is the evidence, and it points to an expired Calico credential. The intended fix, restarting the `calico-node` pod, was never run, because the project owner uninstalled microk8s instead, so it is untried.

**Memory and the GC.** Measured on 2026-11-02. The first full scrape ran with `-XX:MaxRAMPercentage=70` everywhere and no GC set. Below 1792 MiB the JVM picks Serial by itself, so all of it ran under Serial, while Compose runs G1. The services then got G1 explicitly, and the G1 columns come from reads. Neither a nightly run's result nor any other scrape of size under G1 and a cap was recorded.

| Pod | First scrape: limit, peak, under Serial | Reads, Serial: peak, heap committed, longest pause | Reads, G1: peak, heap committed, longest pause | Heap still used after a collection, G1 | Limit chosen, `MaxRAMPercentage` | Heap cap | Request |
| --- | --- | --- | --- | --- | --- | --- | --- |
| regional-statistics | 1536 MiB, 1348 MiB | 298, 55, 84 ms | 378, 70, 48 ms | 33 MiB | 1536 MiB, 70% | 1075 MiB | 768 MiB |
| parliament | 1 GiB, 932 MiB | 1131, 852, 227 ms | 688, 467, 25 ms | 245 MiB | 1536 MiB, 70% | 1075 MiB | 512 MiB |
| macro-series | 1 GiB, 389 MiB | not measured | 1074, 788, 37 ms | 681 MiB | 1536 MiB, 70% | 1075 MiB | 384 MiB |
| election | 1 GiB, 467 MiB | not measured | 350, 72, 47 ms | 37 MiB | 1 GiB, 60% | 616 MiB | 384 MiB |
| postgres | 2 GiB, 1347 MiB, mostly page cache (55 MiB its own) | | | | 2 GiB | | 512 MiB |
| frontend | 256 MiB, 17 MiB | | | | 128 MiB | | 32 MiB |

Reads are 84 to 255 requests per service through the gateway. Before this, 26 reads had taken parliament to 1151 MiB under Serial at a 1280 MiB limit. Macro-series' reads include a chart of all 108 series by day, at that heap cap. At a 616 MiB cap the same reads gave 890 MiB, with 582 MiB of heap in use after a collection.

- **Why G1, set explicitly.** Serial lets the old generation fill with garbage until a full collection, so a pod's peak is close to its heap cap however little is live: parliament reached 852 MiB of committed heap under Serial, with at most 245 MiB left after a collection under G1 from the same requests, at a ninth of the longest pause. So a Serial peak says little about what is needed. G1 is also what Compose runs, and its 846 MiB for a first full SCB scrape is a G1 number, on a JVM that could have grown to several GiB and didn't. G1's cost was about 30 MiB more outside the heap on regional-statistics.
- **The rule for limits.** Outside the heap, a pod took 220 to 290 MiB (peak minus committed heap, with both GCs), so the limit must leave at least 380 MiB after the heap cap. At 70% that needs a limit of at least 1267 MiB. A first choice of 768 MiB at 70% (heap 537 MiB, 230 MiB left) broke it: a full heap there would have been an OOM kill, not an `OutOfMemoryError`. 1536 MiB at 70% leaves 461 MiB, and 1 GiB at 60% leaves 410 MiB. `-XX:+ExitOnOutOfMemoryError` makes a heap that runs out show as a restart, and `-Xlog:gc` gives one line per collection. CPU had requests only.
- **Peaks without the metrics addon** are in each pod's `/sys/fs/cgroup/memory.peak`.
- **What isn't known.** A full SCB scrape under G1 with a 1075 MiB cap never ran. Compose's 846 MiB says it fits, but that heap was never constrained, so a fallback to full collections is possible.
- **A large chart is a risk at any limit.** A chart of all 108 series by day held 681 MiB of heap after a collection, and no cache was found in macro-series' code, so that heap looks per request. Two at once would then exceed a 1075 MiB cap, from an unauthenticated POST. That is inferred from these numbers and wasn't tried.

Against [0001](0001-split-backend-into-one-service-per-domain.md)'s estimate of 1.5 to 2 GiB for four JVMs: 1.1 GiB idle just after a restart (under Serial), and the sum of the first scrape's peaks was 3.1 GiB.
