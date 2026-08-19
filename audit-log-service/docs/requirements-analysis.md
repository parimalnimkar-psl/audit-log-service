# Requirements Analysis

## Core requirements
- Retention/archive and structured redaction are documented design scope-outs pending retention rules and key-management approval. Verifiable JSON export is implemented at `GET /audit/export`.

## Implementation decisions
Java 17, Spring Boot, Maven, Spring Data JPA/Hibernate, PostgreSQL, Spring Security/JWT, JUnit 5/Mockito, JaCoCo, OpenAPI, SLF4J/Logback and Actuator/Micrometer are selected implementation technologies. The default H2 profile is supplied for immediate local execution; PostgreSQL is the intended relational deployment profile.

## Important design limitation
A hash chain detects altered/inconsistent records but does not itself prove the identity of a database administrator who changed data. Production requires least-privilege database access, database audit controls, backups and operational monitoring.
