# Audit Log Service

Java 17 Spring Boot service implementing append-only audit events, SHA-256 tamper-evident hash chaining, query/filtering, verification, Keycloak OAuth2/OIDC resource-server authorization, local-test authentication, JPA/Hibernate, Flyway, OpenAPI, logging, Actuator/Micrometer, JUnit 5, Mockito and JaCoCo.

## Quick start
```bash
mvn clean verify
mvn spring-boot:run
```
Default profile uses in-memory H2 so the application starts immediately.

Open:
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

## Local development authentication
POST `/auth/token` with:
```json
{"username":"writer","password":"writer123"}
```
Users: writer/writer123 -> AUDIT_WRITER,AUDIT_READER; admin/admin123 -> all roles; reader/reader123 -> AUDIT_READER.

Use returned token as `Authorization: Bearer <token>`.

## Keycloak/OIDC deployment

Production authentication uses the `keycloak` profile and does not expose the local `/auth/token` credential endpoint:

```bash
set SPRING_PROFILES_ACTIVE=keycloak
set KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/audit
mvn spring-boot:run
```

The issuer must provide realm roles `AUDIT_READER`, `AUDIT_WRITER`, `AUDIT_ADMIN`, and `AUDIT_EXPORTER` (the converter also accepts the `ROLE_`-prefixed form). Configure the Keycloak client and TLS/secret management in the deployment environment.

## Export and pagination

- `GET /audit/events?page=0&size=20` supports bounded pagination; the maximum page size is 200.
- `GET /audit/export` is restricted to `AUDIT_EXPORTER` or `AUDIT_ADMIN` and returns JSON records with SHA-256, genesis, sequence, and chain-hash metadata.
- Audit and user API traffic is rate-limited per client IP; throttled requests return `429` with `Retry-After: 60`.

## PostgreSQL
Create database/user using `database/postgresql/01-create-database.sql`, then run with:
```bash
set SPRING_PROFILES_ACTIVE=postgres
set DB_USERNAME=audit_user
set DB_PASSWORD=change_me
mvn spring-boot:run
```
See `database/README.md`.

> Demo credentials and JWT secret are for local development only. Replace with an identity provider and secure secrets in production.
