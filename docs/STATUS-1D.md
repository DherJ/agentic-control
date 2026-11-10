# Increment 1D — Artifact and human approval foundation

- Reuses v1.1 `artifact` and `approval` tables, adding step_execution_id via Liquibase 005.
- Immutable artifact content/version, SHA-256 hash, read-only REST endpoints.
- Approval request/decision service, optimistic version check, mandatory rejection comment, audit events.
- Workflow ownership authorization inherited from 1C; CSRF remains enabled.

## Deliberate limitations (to complete in 1E)

- Agent execution and artifact recording have **no public API**. Internal service methods are integration hooks only.
- Approval decision currently records a decision but **does not advance workflow steps or run rejection loops**. Do not interpret it as an end-to-end approval engine.
- Idempotency-Key handling, stale downstream artifact invalidation, step/type binding and full output schema validation remain to be implemented before production.
- The 1C scheduler can reach WAITING_APPROVAL without creating an approval record; this integration is part of 1E.
- No Maven/JDK25 build verification in this environment.
