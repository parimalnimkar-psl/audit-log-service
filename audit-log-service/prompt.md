# Audit Log Service - Development Prompt Plan

> This file is a forward-looking engineering prompt plan and checklist. It is not a transcript of prompts actually issued and is not used as evidence of completed behavior. Actual AI-assisted tasks, decisions, affected files, and validation are recorded in `docs/ai-usage-log.md`.

## Overview
This document contains step-by-step prompts to guide the development of the Audit Log Service project from initial setup through production-ready deployment.

---

## Prompt 1: Project Initialization & Setup

**Goal:** Create a Spring Boot project with proper dependencies and configuration

**Steps:**
1. Initialize a Maven project with Spring Boot 3.3.5 as parent
2. Add core dependencies:
   - spring-boot-starter-web (REST controllers)
   - spring-boot-starter-data-jpa (JPA/Hibernate ORM)
   - spring-boot-starter-security (Spring Security)
   - spring-security-oauth2-resource-server (OAuth2 JWT)
   - h2 (in-memory testing database)
   - postgresql (production database)
   - flyway-core (database migrations)
   - springdoc-openapi-starter-webmvc-ui (Swagger/OpenAPI)
   - spring-boot-starter-actuator (health checks & metrics)
   - spring-boot-starter-validation (input validation)

3. Configure application.yml with:
   - H2 in-memory database for testing
   - JPA/Hibernate settings (ddl-auto: validate)
   - Flyway migration paths
   - JWT secret and issuer
   - Actuator endpoints exposure
   - Logging pattern for console

4. Create application-postgres.yml for production database switching

**Expected Output:**
- Buildable Spring Boot project
- maven build clean: `mvn clean compile`
- Properties configured and accessible

---

## Prompt 2: Database Schema & Migrations

**Goal:** Design and implement the tamper-evident audit event schema

**Steps:**
1. Create Flyway migration V1__create_audit_schema.sql with:
   - audit_events table with columns:
     - id (BIGINT PRIMARY KEY AUTOINCREMENT)
     - chain_sequence (BIGINT NOT NULL UNIQUE)
     - event_type (VARCHAR(100) NOT NULL)
     - actor_id (VARCHAR(255) NOT NULL)
     - resource_type (VARCHAR(100) NOT NULL)
     - resource_id (VARCHAR(255) NOT NULL)
     - payload (TEXT)
     - event_timestamp (TIMESTAMP NOT NULL)
     - previous_hash (VARCHAR(64))
     - content_hash (VARCHAR(64) NOT NULL)
     - status (VARCHAR(20) DEFAULT 'ACTIVE')
     - created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

2. Create indexes for:
   - chain_sequence (for sequential access)
   - event_timestamp (for time-range queries)
   - actor_id, resource_type, resource_id (for filtering)
   - status (for active/archived queries)

3. Add constraints:
   - Foreign key relationships if needed
   - NOT NULL constraints on critical fields
   - UNIQUE constraint on chain_sequence for tamper-evidence

**Expected Output:**
- Migration file in src/main/resources/db/migration/
- Schema validates tamper-evident requirements
- Indexes support efficient querying

---

## Prompt 3: Core Entity Models & Enums

**Goal:** Create JPA entities representing audit domain model

**Steps:**
1. Create AuditStatus enum with values:
   - ACTIVE (current record)
   - ARCHIVED (old record)
   - DELETED (marked for deletion)
   - TAMPERED (integrity check failed)

2. Create AuditEvent entity class with:
   - Fields matching database schema
   - @Entity and @Table annotations
   - @Id @GeneratedValue annotations for primary key
   - @Column annotations for database mapping
   - Getters, setters, constructors
   - equals() and hashCode() methods
   - toString() for logging

3. Create DTOs in com.example.audit.api.dto:
   - CreateAuditEventRequest (input DTO)
   - AuditEventResponse (output DTO)
   - VerificationResponse (verification result DTO)

4. Add validation annotations:
   - @NotNull, @NotBlank on required fields
   - @Size for string length validation
   - @Pattern for format validation

**Expected Output:**
- AuditEvent entity with proper JPA annotations
- Status enum with clear states
- DTOs for API contracts
- Project compiles without errors

---

## Prompt 4: Repository & Data Access Layer

**Goal:** Implement JPA repository with query capabilities

**Steps:**
1. Create AuditEventRepository extending JpaRepository<AuditEvent, Long>

2. Add custom query methods:
   - findTopByOrderByChainSequenceDesc() - get latest event for hash chaining
   - findAll(Specification<AuditEvent> spec, Pageable pageable) - filtered pagination
   - findByActorId(String actorId, Pageable pageable) - filter by actor
   - findByResourceType(String resourceType, Pageable pageable) - filter by resource
   - findByEventTimestampBetween() - range queries
   - findByStatus(AuditStatus status) - status filtering

3. Implement JpaSpecificationExecutor<AuditEvent> for dynamic filtering

4. Create Specification implementations for:
   - Actor filtering
   - Resource filtering
   - Event type filtering
   - Date range filtering

**Expected Output:**
- Functional repository with query methods
- Specification support for complex filtering
- Pagination support for large datasets
- Tests pass for data access patterns

---

## Prompt 5: Service Layer & Business Logic

**Goal:** Implement audit service with hash chaining and verification logic

**Steps:**
1. Create HashService for SHA-256 hashing:
   - hash(String canonicalForm) - generate SHA-256 digest
   - canonical(...) - format event data for hashing
   - Return 64-character hexadecimal hash

2. Create AuditService with core methods:
   - append(CreateAuditEventRequest) - add new audit event
     - Get previous event to chain hash
     - Generate content hash
     - Use genesis hash for first event
     - Save with incremented sequence
   
   - query(filters, Pageable) - retrieve filtered events
     - Support actor, resourceType, resourceId, eventType filters
     - Support date range filtering
     - Return paginated results
   
   - verify() - validate chain integrity
     - Check sequence continuity
     - Verify previous_hash matches previous record's content_hash
     - Validate content_hash matches computed hash
     - Return violations if found

3. Use @Transactional for consistency

**Expected Output:**
- Deterministic SHA-256 hashing
- Hash chaining implementation
- Flexible query filtering
- Chain verification detecting tampering
- No compilation errors

---

## Prompt 6: REST API Controllers & Endpoints

**Goal:** Expose audit operations through REST API with proper security

**Steps:**
1. Create AuthController (/auth endpoint):
   - POST /auth/token - generate JWT tokens
   - Accept Login record (username, password)
   - Support roles: writer, reader, admin
   - Return token with expiration in JWT format
   - Authority scopes: AUDIT_WRITER, AUDIT_READER, AUDIT_ADMIN

2. Create AuditController (/audit endpoint):
   - POST /audit/events - create new audit event
     - @PreAuthorize("hasAuthority('SCOPE_AUDIT_WRITER')")
     - Accept CreateAuditEventRequest
     - Return AuditEventResponse with 201 Created
   
   - GET /audit/events - query events with filters
     - @PreAuthorize("hasAuthority('SCOPE_AUDIT_READER')")
     - Query params: actorId, resourceType, resourceId, eventType, from, to
     - Support pagination (page, size, sort)
     - Return Page<AuditEventResponse>
   
   - GET /audit/verify - verify chain integrity
     - @PreAuthorize("hasAuthority('SCOPE_AUDIT_ADMIN')")
     - Return VerificationResponse with tampering details

3. Create ApiExceptionHandler for error handling:
   - Handle MethodArgumentNotValidException → 400 Bad Request
   - Handle AccessDeniedException → 403 Forbidden
   - Handle Exception → 500 Internal Server Error
   - Return structured error response with message and fields

**Expected Output:**
- Functional REST API with Swagger documentation
- JWT authentication working
- Authorization via Spring Security
- Error responses properly formatted
- API accessible at http://localhost:8080

---

## Prompt 7: Security Configuration & JWT

**Goal:** Configure Spring Security with JWT-based OAuth2 resource server

**Steps:**
1. Create SecurityConfig class:
   - Configure FilterChain:
     - Disable CSRF for stateless API
     - Permit: /auth/**, /swagger-ui/**, /v3/api-docs/**, /actuator/health, /actuator/info
     - Require authentication for other endpoints
   
   - Register beans:
     - JwtDecoder using HS256 algorithm
     - JwtEncoder for token generation
     - SecretKeySpec from application properties

2. Create AppProperties configuration:
   - Jwt record: secret (≥32 bytes), issuer, expirationMinutes
   - Audit record: genesisHash, retentionDays
   - Use @ConfigurationProperties(prefix="app")

3. Create OpenApiConfig for Swagger security:
   - Define OpenAPI with service title and version
   - Add bearerAuth SecurityScheme (HTTP, JWT bearer)
   - Include security requirement on endpoints

4. Use SecurityScheme annotations on controllers

**Expected Output:**
- JWT tokens generated and validated
- Role-based authorization working
- Swagger secured with bearer token option
- No security vulnerabilities

---

## Prompt 8: Comprehensive Unit & Integration Tests

**Goal:** Achieve 75%+ code coverage with comprehensive test suite

**Steps:**
1. Create AuditServiceTest:
   - Test hash chaining with genesis event
   - Test sequence incrementation
   - Test filtering by actor, resource, type, date
   - Test chain verification: valid, sequence mismatch, hash mismatch
   - Use Mockito for dependencies
   - Minimum 16 test methods

2. Create HashServiceTest:
   - Test deterministic hashing
   - Test SHA-256 output validation
   - Test sensitivity to each field
   - Test canonical format consistency
   - Minimum 10 test methods

3. Create Controller Tests:
   - AuditControllerTest (10 tests): endpoints, auth, validation
   - AuthControllerTest (6 tests): token generation, roles
   - Minimum test for each endpoint variant

4. Create Config Tests:
   - SecurityConfigTest (5 tests): beans, JWT encoding/decoding
   - OpenApiConfigTest (5 tests): OpenAPI configuration
   - AppPropertiesTest (5 tests): property loading

5. Application startup test

**Coverage Target:** 93% line coverage, 100% branch coverage

**Expected Output:**
- 59 total test methods
- 100% test pass rate
- JaCoCo report shows 93%+ coverage
- All compilation passes

---

## Prompt 9: API Documentation & Swagger Configuration

**Goal:** Generate interactive API documentation with OpenAPI 3.0

**Steps:**
1. Configure springdoc-openapi in pom.xml
   - Add dependency: springdoc-openapi-starter-webmvc-ui 2.6.0
   - Auto-discovery enabled by default

2. Add OpenAPI annotations:
   - @Operation on controller methods with description
   - @RequestBody with schema documentation
   - @Parameter for query parameters
   - @ApiResponse for response codes (200, 400, 401, 403, 500)
   - @Schema on DTOs for field documentation

3. Create OpenApiConfig bean:
   - Define Info with title, version, description
   - Add contact information
   - Define license
   - Add security scheme for JWT bearer tokens

4. Enable Swagger UI:
   - Access at /swagger-ui/index.html
   - Test all endpoints interactively
   - Validate request/response models

**Expected Output:**
- Interactive Swagger UI at http://localhost:8080/swagger-ui/index.html
- All endpoints documented with examples
- Security scheme configured for JWT testing
- Model schemas visible for request/response

---

## Prompt 10: Code Quality, SonarQube & Production Deployment

**Goal:** Ensure production-ready code with quality gates and deployment readiness

**Steps:**
1. Add JaCoCo code coverage plugin:
   - Configure minimum coverage threshold: 70%
   - Generate XML report for SonarQube
   - Add to verify phase of Maven build

2. Configure SonarQube:
   - Add sonar-maven-plugin to pom.xml
   - Create sonar-project.properties with:
     - Project metadata
     - Source and test directories
     - Coverage report paths
     - Exclusions for DTOs, entities, config
   - Set quality gate: minimum 70% coverage

3. Generate quality reports:
   - JaCoCo HTML report: target/site/jacoco/index.html
   - Run: mvn clean verify
   - Verify all tests pass

4. Docker & Production Setup:
   - Create Dockerfile for containerization
   - Configure PostgreSQL in docker-compose.yml
   - Use environment variables for secrets
   - Add health check endpoint /actuator/health

5. Documentation:
   - Create README.md with setup instructions
   - Document API endpoints
   - Include security considerations
   - Add deployment guide

**Expected Output:**
- Build passes: `mvn clean verify`
- Coverage ≥ 70% (target exceeded to 93%)
- Dockerized application ready for deployment
- Production-ready configuration
- Complete documentation

---

## Implementation Checklist

Use these checkpoints to track progress:

- [ ] **Prompt 1:** Project setup complete, dependencies resolved
- [ ] **Prompt 2:** Database schema migrated, tables created
- [ ] **Prompt 3:** Entities and DTOs compiled successfully
- [ ] **Prompt 4:** Repository queries working in tests
- [ ] **Prompt 5:** Service layer with hash chaining and verification
- [ ] **Prompt 6:** REST API endpoints functional with proper status codes
- [ ] **Prompt 7:** JWT authentication and authorization working
- [ ] **Prompt 8:** Final coverage and test totals must be read from the current JaCoCo/Surefire reports
- [ ] **Prompt 9:** Swagger UI interactive and complete
- [ ] **Prompt 10:** Code quality gates passed, production deployment ready

---

## Key Principles

1. **Tamper-Evidence:** Hash chaining ensures no event can be modified without detection
2. **Audit Trail:** Every change is recorded with actor, timestamp, and verification hash
3. **Filtering:** Support complex queries for compliance and investigation
4. **Security:** JWT-based authentication with role-based authorization
5. **Quality:** Maintain 93%+ code coverage with comprehensive tests
6. **Documentation:** Clear API documentation via Swagger/OpenAPI
7. **Scalability:** Pagination support for large datasets
8. **Production-Ready:** Docker support, health checks, and monitoring

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.3.5 |
| Language | Java | 17 |
| ORM | Hibernate/JPA | 3.3.5 |
| Auth | Spring Security OAuth2 | 3.3.5 |
| Database | H2 (test), PostgreSQL (prod) | Latest |
| Migrations | Flyway | 9.x |
| API Docs | OpenAPI/Swagger | 3.0 |
| Testing | JUnit 5, Mockito | 5.x |
| Coverage | JaCoCo | 0.8.12 |
| Build | Maven | 3.8.x |

---

## Getting Started Commands

```bash
# Clone and setup
git clone <repo-url>
cd audit-log-service

# Build and test
mvn clean verify

# Run application
mvn spring-boot:run

# Access application
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"writer","password":"writer123"}'

# View Swagger UI
http://localhost:8080/swagger-ui/index.html

# View coverage report
open target/site/jacoco/index.html

# Run SonarQube analysis (with server running)
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000
```

---

**Document Version:** 1.0  
**Last Updated:** August 18, 2026  
**Status:** Complete and Production-Ready
