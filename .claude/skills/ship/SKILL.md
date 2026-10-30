---
name: ship
description: Ship a change the user has already agreed on. An implementer sub-agent builds it and opens a PR, an independent reviewer sub-agent reviews it, and the PR is merged once CI passes. Do not use while the change is still being discussed or designed.
---

# Ship an agreed change

You orchestrate this workflow. The sub-agents write the code and the review.

The sub-agents are plain subagents, not agent-team teammates: `.claude/settings.json` sets `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=0`, so each one reports once, when its turn ends, and stays reachable by name with SendMessage for later rounds.

## 1. Preconditions

- The user has agreed on the change. If the design or scope is still open, settle it with the user first.
- Run `docker ps` and note which `macrohub-*` containers are running. Step 7 uses this list.

## 2. Implementer (named sub-agent, reachable with SendMessage)

Its brief must include the agreed design, what is out of scope, and these rules:

- Work in your own git worktree under your scratchpad directory, on a feature branch from an up-to-date `origin/main`: `git -C <repo> fetch && git -C <repo> worktree add <dir> -b <branch> origin/main`. A ruleset blocks direct pushes to `main`.
- Before every push, run the checks for what you changed:
  - Backend: `JAVA_HOME=~/.sdkman/candidates/java/current mvn test` from `macro-backend/macro-analyzer/`, with Docker running.
  - Frontend: `PATH=~/.nvm/versions/node/v22.22.1/bin:$PATH npm run build` and `npm run lint` from `macro-frontend/`. Lint must exit 0, as CI enforces.
- Run builds, tests and other checks in the foreground with a long `timeout` (up to 600000 ms); they all fit in it. Don't start them with `run_in_background` and wait for a completion notice: an idle sub-agent is not reliably woken by it and stalls. For anything longer, use Monitor with a loop that ends when the process exits.
- For frontend changes that add assets, workers or routes, also check the production build as nginx serves it: build the image under a throwaway tag, run it on a spare port, and `curl -I` the new assets. Vite's dev server hides serving bugs, e.g. nginx served `.mjs` as `application/octet-stream`, which blanked the MapLibre map.
- Use conventional commits that end with the Co-Authored-By line. Open the PR with `gh pr create` and end its body with the Claude Code line. Wait for CI with `gh pr checks <n> --watch` and fix any failing job.
- After `gh pr checks <n> --watch` and before you report the PR ready, handle the automatic `claude[bot]` review from `.github/workflows/claude-review.yml`. Its `claude-review` job is one of the checks, so its comments exist once the watch ends. Read the summary (`gh pr view <n> --comments`) and inline comments (`gh api repos/{owner}/{repo}/pulls/<n>/comments`) for the current head. Do the same after every later push; the bot re-reviews each one. A PR that changes `claude-review.yml` itself gets no bot review.
- When asked to address a review on the PR, read the findings yourself from the PR: review bodies and summary comments with `gh pr view <n> --comments`, inline comments with `gh api repos/{owner}/{repo}/pulls/<n>/comments`.
- Fix every reviewer and `claude[bot]` finding by default, except a nit the lead tells you to drop. Report only those that conflict with the agreed requirements or that you judge wrong, each with a one-line reason.
- Your final answer is your report: PR number, head SHA, check results, and open questions or conflicts, if any. Don't also send it with SendMessage.
- Don't merge. Stay available for review rounds.

## 3. Reviewer (fresh named sub-agent)

- Spawn it with `model: "fable"`. If that spawn (or a later re-review message) fails because Fable credits or usage limits are exhausted, or the model is unavailable, spawn a fresh reviewer with `model: "opus"` and the same brief, and tell the user the review ran on Opus.
- Brief it with the PR number, the stated intent and the agreed requirements. Leave out the implementer's reasoning, the `claude[bot]` comments and your own opinions. It may see them on the PR, but it forms its own view.
- It reviews `gh pr diff <n>` and reads surrounding code for context.
- If it builds, it uses its own worktree. It never checks out branches in the main clone.
- It follows the implementer's rule for long-running commands: run them in the foreground with a long `timeout`, or poll with Monitor. It never waits for a background completion notice.
- It labels each finding blocking or nit and posts them with `gh pr review <n> --comment`. It cannot approve, because it runs on the PR author's account. The review starts with exactly one verdict:
  - `LGTM`: merge as is.
  - `LGTM after nits`: no blocking findings; merge once the listed nits are fixed, without another review.
  - `Changes requested`: at least one blocking finding; re-review after the fixes.
- Its final answer is the verdict plus a one-line summary per finding. It doesn't also send it with SendMessage.

## 4. Iterate

- The PR is the single source of truth for findings; don't retype them. Send the implementer a short message such as "address the review on #<n>", plus any nit to drop and why. It fixes all the others by default.
- Ask the user about requirement or product questions, including conflicts the implementer reports. Don't decide them yourself.
- After the fixes are pushed:
  - `LGTM after nits`: read the new commits' diff yourself. If they do what the nits asked and nothing else, go to step 6. Otherwise ask the reviewer to re-review.
  - `Changes requested`: ask the reviewer to re-review the new commits.
- Watch for stalls. When a sub-agent's turn ends without the expected result (no review on the PR in `gh pr view <n> --json reviews`, or no PR or push), message it at once to check its command's result itself and finish. If it stalls again, take over the check yourself.

## 5. Cap

If blocking findings remain after 3 fix rounds, stop and bring the disagreement to the user.

## 6. Merge

On `LGTM` (or `LGTM after nits` with the nits checked), first remove the sub-agents' worktrees (`git worktree remove <dir>`), or `--delete-branch` fails on the branch still checked out there. Check that the `frontend` and `backend` CI checks pass on the head commit (`gh pr checks <n>`); `main` requires them. Then merge without asking: `gh pr merge <n> --squash --delete-branch --match-head-commit <sha>`. Don't use `--admin`; if a check fails, send it back to the implementer. Then tell the user what landed, what wasn't verified (e.g. nothing was tried in a browser) and any open follow-ups.

## 7. After merge

From the repo root of the main clone:

1. Run `git pull` on `main`.
2. Rebuild and restart only the containers the change affects, and only if step 1 found them running. Never start containers that weren't running.
   - If the backend or frontend runs outside Docker (`spring-boot:run` or Vite; check `lsof -nP -iTCP -sTCP:LISTEN | grep -E ':(8080|8081)'`), don't restart it. Tell the user it keeps serving the old code until they restart it.
   - Frontend: `docker compose --env-file macro-backend/macro-analyzer/.env -f macro-backend/macro-infra/docker/docker-compose-all.yml up -d --build --no-deps macro-frontend`
   - Backend: from `macro-backend/macro-analyzer/`, run `sh ./mvnw clean package && docker build -t macro-analyzer .`. Then, from the repo root, run the same compose command with `up -d --no-deps macro-analyzer`.
3. Check with curl that each restarted service responds: `http://localhost:8081` for the frontend, `http://localhost:8080/macro-analyzer/series-config` for the backend.
4. Once the change is in `main`, stop the implementer and reviewer sub-agents with TaskStop so none linger. Spawn fresh ones for the next change.
