# AI Usage Log

This log records the material AI-assisted work performed during development. Entries identify the affected files/classes, the engineering decision, and the validation used. `prompt.md` is a forward-looking development guide, not a transcript of prompts.

| Date | Task | AI Tool | Prompt Summary | Decision | Engineer Changes | Validation |
|---|---|---|---|---|---|---|
| 2026-08-17 | Initial project structure | AI coding assistant | Design Spring Boot/Maven audit service with JPA, Flyway, security, and OpenAPI | Accepted after review | `pom.xml`, `application.yml`, application bootstrap, package boundaries | Compile and application-context tests |
| 2026-08-17 | Audit chain and persistence | AI coding assistant | Define append/query/verify model, SHA-256 canonical form, migrations, and repository | Modified after review | `AuditEvent`, `HashService`, `AuditService`, `AuditEventRepository`, `V1__create_audit_schema.sql` | Hash/service tests and Flyway-backed H2 integration tests |
| 2026-08-17 | Authentication and authorization | AI coding assistant | Implement local development login, BCrypt users, scopes, method security, and admin management | Modified after review | `AuthController`, `SecurityConfig`, `UserService`, `UserController`, `V2__create_users_table.sql` | Auth, user-service, controller authorization, and JWT round-trip tests |
| 2026-08-18 | Security remediation | AI coding assistant | Close H2 exposure, canonicalization, actor attribution, actuator exposure, and sequence-allocation findings | Modified after review | `SecurityConfig`, `AuditController`, `AuditService`, `HashService`, `application.yml`, DTO validation | Negative security tests, SQL tamper test, concurrency integration test, and live probes |
| 2026-08-19 | Keycloak/export/API controls | AI coding assistant | Add Keycloak/OIDC profile, JSON export, pagination cap, and API rate limiting | Modified after review | `KeycloakSecurityConfig`, `KeycloakAuthorityConverter`, `application-keycloak.yml`, `AuditExportResponse`, `ApiRateLimitFilter` | Focused API tests and live export/authorization/pagination checks |
| 2026-08-19 | Test and evidence remediation | AI coding assistant | Add integration, malformed-input, wrong-scope, rate-limit, and JWT tests; reconcile documentation | Modified after review | `AuditPersistenceIntegrationTest`, `ApiRateLimitFilterTest`, `AuditControllerTest`, `SecurityConfigTest`, attestation/docs | Current clean `mvn clean verify`: 93 tests, 0 failures/errors/skips; `target/surefire-reports/TEST-*.xml` and `target/site/jacoco/jacoco.xml`; no external Keycloak server available locally |

## Evidence Note

The final validation claims are based on generated Surefire XML and JaCoCo XML artifacts, not on historical prose summaries. The current run reports 93 tests, 0 failures, 0 errors, 0 skipped, 83.5% line coverage, and 69.3% branch coverage. The historical `security-test-output.txt` records the earlier circular-dependency failure and is retained as diagnostic history.
