# Audit Log Service

Java 17 Spring Boot service implementing append-only audit events, SHA-256 tamper-evident hash chaining, query/filtering, verification, JWT authentication/authorization, JPA/Hibernate, Flyway, OpenAPI, logging, Actuator/Micrometer, JUnit 5, Mockito and JaCoCo.

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

## Demo JWT
POST `/auth/token` with:
```json
{"username":"writer","password":"writer123"}
```
Users: writer/writer123 -> AUDIT_WRITER,AUDIT_READER; admin/admin123 -> all roles; reader/reader123 -> AUDIT_READER.

Use returned token as `Authorization: Bearer <token>`.

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
