---
name: ship
description: Ship a change the user has already agreed on. An implementer sub-agent builds it and opens a PR, an independent reviewer sub-agent reviews it, and the PR is admin-merged. Do not use while the change is still being discussed or designed.
---

# Ship an agreed change

You orchestrate this workflow. The sub-agents write the code and the review.

## 1. Preconditions

- The user has agreed on the change. If the design or scope is still open, settle it with the user first.
- Run `docker ps` and note which `macrohub-*` containers are running. Step 7 uses this list.

## 2. Implementer (named sub-agent, reachable with SendMessage)

Its brief must include the agreed design, what is out of scope, and these rules:

- Work in your own git worktree under your scratchpad directory, on a feature branch from an up-to-date `origin/main`: `git -C <repo> fetch && git -C <repo> worktree add <dir> -b <branch> origin/main`. A ruleset blocks direct pushes to `main`.
- Before every push, run the checks for what you changed:
  - Backend: `JAVA_HOME=~/.sdkman/candidates/java/current mvn test` from `macro-backend/macro-analyzer/`, with Docker running.
  - Frontend: `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run build` and `npm run lint` from `macro-frontend/`. Lint already has errors on `main`, so compare against `main` and add no new ones.
- Use conventional commits that end with the Co-Authored-By line. Open the PR with `gh pr create` and end its body with the Claude Code line.
- Don't merge. Stay available for review rounds.

## 3. Reviewer (fresh named sub-agent)

- Brief it with the PR number, the stated intent and the agreed requirements. Leave out the implementer's reasoning and your own opinions.
- It reviews `gh pr diff <n>` and reads surrounding code for context.
- If it builds, it uses its own worktree. It never checks out branches in the main clone.
- It labels each finding blocking or nit and posts them with `gh pr review <n> --comment`, starting with "LGTM" or "Changes requested". It cannot approve, because it runs on the PR author's account.

## 4. Iterate

- Forward the findings to the implementer. Decide which nits to fix and which to accept.
- Ask the user about requirement or product questions. Don't decide them yourself.
- After the fixes are pushed, ask the reviewer to re-review the new commits.

## 5. Cap

If blocking findings remain after 3 fix rounds, stop and bring the disagreement to the user.

## 6. Merge

On LGTM, merge without asking: `gh pr merge <n> --admin --squash --delete-branch --match-head-commit <sha>`. Then tell the user what landed, what wasn't verified (e.g. nothing was tried in a browser) and any open follow-ups.

## 7. After merge

From the repo root of the main clone:

1. Run `git pull` on `main`.
2. Rebuild and restart only the containers the change affects, and only if step 1 found them running. Never start containers that weren't running.
   - Frontend: `docker compose --env-file macro-backend/macro-analyzer/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --build --no-deps macro-frontend`
   - Backend: from `macro-backend/macro-analyzer/`, run `sh ./mvnw clean package && docker build -t macro-analyzer .`. Then, from the repo root, run the same compose command with `up -d --no-deps macro-analyzer`.
3. Check with curl that each restarted service responds: `http://localhost:8081` for the frontend, `http://localhost:8080/macro-analyzer/series-config` for the backend.
