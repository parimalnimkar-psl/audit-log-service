# AI Usage Log

This log records the AI-assisted work performed during development of the audit log service.

| Date | Task | AI Tool | Prompt Summary | Decision | Engineer Changes | Validation |
|---|---|---|---|---|---|---|
| 2026-08-17 | Initial project structure and Spring Boot scaffolding | AI coding assistant | Create Maven/Spring Boot project for an append-only audit log service with JWT auth, JPA, Flyway, and Swagger | Accepted | Scaffolded the project, dependencies, and base configuration | Maven project compiled successfully |
| 2026-08-17 | Domain and persistence design | AI coding assistant | Define audit event schema, hash chain model, and repository structure | Accepted | Added audit event entity, repository, and migration schema | Flyway validation and targeted tests passed |
| 2026-08-17 | Security and auth design | AI coding assistant | Implement JWT login, user role scopes, and method-level authorization | Accepted | Added security configuration, JWT encoder/decoder, and user auth flow | AuthController integration tests passed |
| 2026-08-18 | Hardening and remediation | AI coding assistant | Fix review findings around H2 console exposure, hash escape handling, and audit API contract mismatches | Modified | Hardened security config, corrected JWT response fields, fixed migration SQL, and aligned exemptions | Focused regression suite passed |
| 2026-08-19 | Final validation | AI coding assistant | Validate the app under focused API/security regression tests | Modified | Reconciled the validation evidence with the executable suite and live API checks | Final result recorded after the remediation run, not the earlier 35-test placeholder |
| 2026-08-19 | Review remediation | AI coding assistant | Replace local JWT deployment authentication with Keycloak OAuth2/OIDC, add export, rate limiting, pagination controls, and persistence integration tests | Modified | Added the `keycloak` profile, realm-role conversion, admin/export authorization, bounded API rate limiting, JSON export metadata, and real H2 persistence tests | `mvn clean verify` and live health/security/export checks |
