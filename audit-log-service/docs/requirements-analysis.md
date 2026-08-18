# Requirements Analysis

## Core requirements
- Append-only audit event creation with event type, actor, resource, payload and timestamp.
- Query filtering by actor/resource/event type/time range with pagination.
- Tamper-evident hash chain: each event has its content hash and previous hash; first event uses a genesis value.
- Verification identifies whether the chain is intact and reports the first inconsistency.
- Demonstrate tampering by direct datastore modification and subsequent verification.
- Retention/archive, structured redaction and verifiable export are addressed by the design.
- Compliance reporting requirement must be clarified before final implementation.

## Implementation decisions
Java 17, Spring Boot, Maven, Spring Data JPA/Hibernate, PostgreSQL, Spring Security/JWT, JUnit 5/Mockito, JaCoCo, OpenAPI, SLF4J/Logback and Actuator/Micrometer are selected implementation technologies. The default H2 profile is supplied for immediate local execution; PostgreSQL is the intended relational deployment profile.

## Important design limitation
A hash chain detects altered/inconsistent records but does not itself prove the identity of a database administrator who changed data. Production requires least-privilege database access, database audit controls, backups and operational monitoring.
