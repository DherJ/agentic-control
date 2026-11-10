# Runbook - local validation (Windows personal PC)

Prerequisites: Docker Desktop / Docker Compose v2. Java 25 and Maven are optional for container build.

1. Copy `infra/.env.example` to `infra/.env`, replace both passwords with long random secrets.
2. From `infra/`: `docker compose --profile local up --build -d`
3. `docker compose --profile local ps`
4. `docker compose logs --tail=150 backend` and check Liquibase and startup logs.
5. Open `http://localhost:8088/agent-workspace/` and log in with bootstrap admin.
6. Check the workflow definitions API via the UI; create a project and a BMAD workflow.
7. For simulated agent execution, `SPRING_PROFILES_ACTIVE=dev` is required (local ONLY).
8. To stop: `docker compose --profile local down` (never add `-v` unless intentionally deleting data).

## PostgreSQL backup (local example)

From `infra/`:

`docker compose exec -T postgres pg_dump -U agent_workspace -d agent_workspace -Fc > agent-workspace.backup`

Restore into an EMPTY development database only after checking credentials, backup file, and current state. A tested restore procedure is required before production.

## Production checklist (NOT AUTOMATED)

- Keep `SPRING_PROFILES_ACTIVE=prod`, ensure the simulation endpoint returns 404.
- Keep `Secure` session cookies, TLS termination at existing Nginx, and CSRF enabled.
- Connect the app web network to the existing reverse proxy explicitly; do not expose the database.
- Confirm Nginx upstream service aliases and test `nginx -t` before reload.
- Use strong unique secrets, restrictive file permissions, off-host encrypted backups, and retention.
- Test project access controls, rejection loops, concurrent decisions, and restart recovery.
- Pin container image versions/digests after validation and review dependency updates.
