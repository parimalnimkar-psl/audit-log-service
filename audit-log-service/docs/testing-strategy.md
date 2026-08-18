# Testing Strategy
- JUnit 5: domain and service logic.
- Mockito: repository isolation and service behavior.
- Spring Boot integration testing: application context and API/security boundaries.
- Tamper tests: modify persisted content and verify failure.
- JaCoCo: report generated at `target/site/jacoco/index.html`; build gate is 70% line coverage in this starter.

Run: `mvn clean verify`.
