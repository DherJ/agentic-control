# Lot 1G - Integration and packaging

## Delivered
- Separate PostgreSQL 17 container with durable volume, no published database port.
- Spring Boot 4 / Java 25 multi-stage image, React/Vite build image, internal Nginx static server.
- Optional loopback-only gateway for local testing at http://localhost:8088/agent-workspace/.
- API session login adapted for React: CSRF-protected POST /api/v1/auth/login and JSON 401 errors.
- Java regex fix in DefinitionValidator (literal dots with `[.]`).
- Development cookie override for local HTTP only; production keeps Secure cookie.
- Nginx reverse proxy example for existing VPS gateway (manual review required).
- Smoke-test instructions and backup/restore runbook.

## Not verified here
- Java 25/Maven build, PostgreSQL integration, Docker Compose startup, end-to-end UX flow.
- Production deployment and live proxy configuration.
- All earlier 1E business invariants (rejection loops, concurrency, idempotency) need integration tests.

## Known limitations
- This is an integration candidate, not a production release.
- The dev profile exposes simulated agents: never use it on a public deployment.
- Production readiness requires full integration testing, secrets handling, restore drills, and review of security authorization.
