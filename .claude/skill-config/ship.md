# `ship` config: macro-hub

Read by the `ship` skill. Everything here applies only to this repository.

## Before starting

- Run `docker ps` and note which `macrohub-*` containers are running. *After merge* uses this list.

## Repository

- The default branch is `main`. A ruleset blocks direct pushes to it.
- `.claude/settings.json` sets `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=0`, so the sub-agents are plain subagents.

## Checks before every push

Run the checks for what you changed. They all fit in the 600000 ms foreground timeout.

- Backend: `JAVA_HOME=~/.sdkman/candidates/java/current mvn test` from `macro-backend/`, with Docker running.
- Backend Java: follow the `immutable-java` skill (`~/.claude/skills/immutable-java/SKILL.md`) while writing it, and run its check from the root of your worktree: `python3 -I ~/.claude/skills/immutable-java/check.py $(git diff --name-only origin/main...HEAD)`. It must report no errors. If that path is missing, the skill isn't linked: link it as the agentic-skills README says, rather than skip the check.
- Frontend: `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run build` and `npm run lint` from `macro-frontend/`. Lint must exit 0, as CI enforces.

## Extra checks

- For frontend changes that add assets, workers or routes, also check the production build as nginx serves it: build the image under a throwaway tag, run it on a spare port, and `curl -I` the new assets. Vite's dev server hides serving bugs, e.g. nginx served `.mjs` as `application/octet-stream`, which blanked the MapLibre map.

## Automated PR reviewers

- `claude[bot]`, from `.github/workflows/claude-review.yml`. Its `claude-review` job is one of the checks, so its comments exist once `gh pr checks <n> --watch` ends. It re-reviews every push. A PR that changes `claude-review.yml` itself gets no bot review.

## Review rules

- For backend Java, also check the diff against the `immutable-java` skill's rules and its "When reviewing" section. Name `~/.claude/skills/immutable-java/SKILL.md` and `.claude/skill-config/immutable-java.md` in the reviewer's brief.

## Merge

- `main` requires the `frontend` and `backend` CI checks. Both must pass on the head commit.
- Merge method: squash.

## After merge

Rebuild and restart only the containers the change affects, and only if *Before starting* found them running. Never start containers that weren't running.

- If the backend or frontend runs outside Docker (`spring-boot:run` or Vite; check `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8082|8083|8084|8085|8086|5173)'`), don't restart it. Tell the user it keeps serving the old code until they restart it.
- Frontend: `docker compose --env-file macro-backend/macro-series-service/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --build --no-deps macro-frontend`
- Backend: from `macro-backend/`, run `sh ./mvnw clean package`. Then `docker build -t regional-statistics-service regional-statistics-service`, `docker build -t macro-series-service macro-series-service`, `docker build -t parliament-service parliament-service` and `docker build -t election-service election-service` and `docker build -t auth-service auth-service` for the services the change affects. A change to `macro-platform` affects all five. Then, from the repo root, run the same compose command with `up -d --no-deps regional-statistics`, `up -d --no-deps macro-series`, `up -d --no-deps parliament`, `up -d --no-deps election` and `up -d --no-deps auth`.

Check with curl that each restarted service responds: `http://localhost:8081` for the frontend, `http://localhost:8082/macro-analyzer/regional-statistics/indicators` for regional-statistics, `http://localhost:8083/macro-analyzer/series-config` for macro-series, `http://localhost:8084/macro-analyzer/parliament/parties` for parliament, `http://localhost:8085/macro-analyzer/municipalities/elections` for election, `http://localhost:8086/macro-analyzer/auth/jwks.json` for auth.
