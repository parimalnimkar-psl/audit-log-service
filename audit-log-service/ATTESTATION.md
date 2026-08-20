# Attestation

## Submission Identity

| Field | Value |
|---|---|
| Assignment | Audit Log Service |
| Candidate | Parimal Nimkar |
| Email | parimalnimkar@gmail.com |
| Repository | https://github.com/parimalnimkar-psl/audit-log-service.git |
| Branch | `main` |
| Commit SHA | `c26947a904f078aff83f49e5616029089546dc98` |
| Development start date | 2026-08-17 |
| Submission date | 2026-08-20 |
| Submission scope | Java 17 / Spring Boot audit-log service, REST APIs, JWT and Keycloak security profiles, H2/PostgreSQL persistence configuration, Flyway migrations, automated tests, live API regression tests, JaCoCo coverage, and supporting documentation |

The branch and commit above identify the repository state reviewed for this submission. Generated build output under `target/` is validation evidence and is not part of the source scope unless explicitly included in the archive.

## Required Attestation

I, Parimal Nimkar, attest that this submission is my own individual work, completed on my own machine and accounts, and that it honestly reflects my development process and use of AI tools. I have recorded material AI-assisted tasks in [docs/ai-usage-log.md](docs/ai-usage-log.md). The claims below are limited to the implementation and evidence listed in this document.

## Claim-to-Evidence Matrix

| Claim | Implementation evidence | Test or runtime evidence | Status |
|---|---|---|---|
| Paginated audit-event queries are implemented | [AuditController.java](src/main/java/com/example/audit/api/AuditController.java) `GET /audit/events`; `Pageable`, filters, and 200-record maximum | [AuditPersistenceIntegrationTest.xml](target/surefire-reports/TEST-com.example.audit.integration.AuditPersistenceIntegrationTest.xml); live `test_all_apis.ps1` pagination and filtering checks | Verified |
| JSON export is implemented | [AuditController.java](src/main/java/com/example/audit/api/AuditController.java) `GET /audit/export`; [AuditExportResponse.java](src/main/java/com/example/audit/api/dto/AuditExportResponse.java) | [AuditPersistenceIntegrationTest.xml](target/surefire-reports/TEST-com.example.audit.integration.AuditPersistenceIntegrationTest.xml); live export check in [test_all_apis.ps1](test_all_apis.ps1) | Verified |
| JWT authentication and role-based authorization are implemented | [SecurityConfig.java](src/main/java/com/example/audit/config/SecurityConfig.java), [KeycloakSecurityConfig.java](src/main/java/com/example/audit/config/KeycloakSecurityConfig.java), and `@PreAuthorize` rules in the API controllers | [SecurityConfigTest.xml](target/surefire-reports/TEST-com.example.audit.config.SecurityConfigTest.xml), [AuthControllerTest.xml](target/surefire-reports/TEST-com.example.audit.api.AuthControllerTest.xml), and live authentication/authorization checks | Verified |
| User administration is restricted to admins | [UserController.java](src/main/java/com/example/audit/api/UserController.java) requires `SCOPE_AUDIT_ADMIN` or `AUDIT_ADMIN` for registration, lookup, listing, role changes, and activation changes | [UserControllerTest.xml](target/surefire-reports/TEST-com.example.audit.api.UserControllerTest.xml) and live reader/admin authorization checks | Verified |
| Supported user levels are reader, writer, and admin | [UserService.java](src/main/java/com/example/audit/service/UserService.java) validates `ROLE_AUDIT_READER`, `ROLE_AUDIT_WRITER`, and `ROLE_ADMIN` | [UserServiceTest.xml](target/surefire-reports/TEST-com.example.audit.service.UserServiceTest.xml) and [AuthControllerTest.xml](target/surefire-reports/TEST-com.example.audit.api.AuthControllerTest.xml) | Verified |
| Audit hash chaining and tamper detection are implemented | [AuditService.java](src/main/java/com/example/audit/service/AuditService.java) and [HashService.java](src/main/java/com/example/audit/service/HashService.java) | [AuditPersistenceIntegrationTest.xml](target/surefire-reports/TEST-com.example.audit.integration.AuditPersistenceIntegrationTest.xml), including persistence, concurrency, and tamper checks | Verified |
| The automated validation gate passes | Maven configuration in [pom.xml](pom.xml) runs Surefire, JaCoCo, and the 70% line-coverage check | `mvn clean verify`; current Surefire XML totals: 93 tests, 0 failures, 0 errors, 0 skipped; current JaCoCo XML: 339/406 lines covered (83.5%), 79/114 branches covered (69.3%) | Verified |

## Validation Reconciliation

The file [security-test-output.txt](security-test-output.txt) contains an older run from 2026-08-19 that failed during Spring context creation because of a circular dependency between `SecurityConfig`, `UserStatusFilter`, and `UserService`. It is retained as historical diagnostic evidence and is not evidence of the final state.

That defect was corrected by moving the password encoder bean into [PasswordEncoderConfig.java](src/main/java/com/example/audit/config/PasswordEncoderConfig.java). The test resource was also completed so Spring tests load the audit and actuator properties. The final `mvn clean verify` run produced the XML artifacts referenced in the matrix above and passed all 93 tests. Earlier HTML reports claiming 75 or 86 passes are historical summaries and are superseded by the current Surefire and JaCoCo XML artifacts.

The live API regression suite was run against a clean packaged service instance and passed 25/25 checks, covering authentication, authorization, event creation, hash chaining, validation, filtering, pagination, export, chain verification, health, Swagger, and OpenAPI.

## Evidence Reproduction

From the project directory containing `pom.xml`:

```powershell
mvn clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File .\test_all_apis.ps1
```

The live API command requires the service to be running on port 8080 with a valid `APP_JWT_SECRET` of at least 32 characters. The machine-readable artifacts are generated at:

- `target/surefire-reports/TEST-*.xml`
- `target/site/jacoco/jacoco.xml`
- `target/site/jacoco/index.html`
