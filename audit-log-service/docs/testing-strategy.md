# Testing Strategy
- JUnit 5: domain and service logic.
- Mockito: repository isolation and service behavior.
- Spring Boot integration testing: application context and API/security boundaries.
- Tamper tests: modify persisted content and verify failure.
- JaCoCo: report generated at `target/site/jacoco/index.html`; build gate is 70% line coverage in this starter.

Run: `mvn clean verify`.

## Current Validation Evidence

The current clean verification run contains 93 `@Test` methods and produced:

- 93 tests executed
- 0 failures, 0 errors, 0 skipped
- 339/406 lines covered (83.5%)
- 79/114 branches covered (69.3%)

Machine-readable evidence is generated under `target/surefire-reports/TEST-*.xml` and `target/site/jacoco/jacoco.xml`; the rendered report is `target/site/jacoco/index.html`. Historical HTML summaries claiming 75 or 86 tests are superseded.

The focused security suite includes `AuditControllerTest`, `AuthControllerTest`, `UserControllerTest`, `SecurityConfigTest`, `ApiRateLimitFilterTest`, and `UserStatusFilterTest`. Persistence coverage includes concurrent ordered appends, content tampering, sequence/constraint failure, pagination, export, and chain verification. Cryptographic redaction commitments remain deferred product scope and are not claimed as implemented tests.
