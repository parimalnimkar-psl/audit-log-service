# AI Usage Log

This log records the AI-assisted work performed during development of the audit log service.

| Date | Task | AI Tool | Prompt Summary | Decision | Engineer Changes | Validation |
|---|---|---|---|---|---|---|
| 2026-08-17 | Initial project structure and Spring Boot scaffolding | AI coding assistant | Create Maven/Spring Boot project for an append-only audit log service with JWT auth, JPA, Flyway, and Swagger | Accepted | Scaffolded the project, dependencies, and base configuration | Maven project compiled successfully |
| 2026-08-17 | Domain and persistence design | AI coding assistant | Define audit event schema, hash chain model, and repository structure | Accepted | Added audit event entity, repository, and migration schema | Flyway validation and targeted tests passed |
| 2026-08-17 | Security and auth design | AI coding assistant | Implement JWT login, user role scopes, and method-level authorization | Accepted | Added security configuration, JWT encoder/decoder, and user auth flow | AuthController integration tests passed |
| 2026-08-18 | Hardening and remediation | AI coding assistant | Fix review findings around H2 console exposure, hash escape handling, and audit API contract mismatches | Modified | Hardened security config, corrected JWT response fields, fixed migration SQL, and aligned exemptions | Focused regression suite passed |
| 2026-08-19 | Final validation | AI coding assistant | Validate the app under focused API/security regression tests | Accepted | Confirmed migration, controller, and security flows operate correctly | Maven test run passed with 35/35 tests green |
