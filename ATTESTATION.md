# ATTESTATION

## Assignment Information

**Assignment Title:**  
Interview Assignment: Build an AI-Assisted Software Engineering System — Audit Log Service

**Candidate Name:**  
Parimal Nimkar

**Candidate Email:**  
parimalnimkar@gmail.com
**Development Start Date:**  
2026-08-17

**Submission Date:**  
2026-08-18

**Repository:**  
https://github.com/parimalnimkar-psl/audit-log-service.git

**Repository Visibility:**  
Private

**Primary Development Environment:**  
Local development machine owned/controlled by the candidate

---

## 1. Required Attestation

I, Parimal Nimkar, attest that this submission is my own individual work, completed on my own machine and accounts, and that it honestly reflects my development process and use of AI.

---

## 2. Ownership and Authorship

I confirm that I personally own and take responsibility for the engineering work contained in this repository.

I am responsible for:

- Understanding and interpreting the requirements.
- Identifying ambiguities and assumptions.
- Designing the system architecture.
- Making technical and architectural decisions.
- Decomposing the requirements into implementation tasks.
- Reviewing and validating generated code.
- Testing the implementation.
- Debugging and resolving defects.
- Reviewing security considerations.
- Reviewing database design and data integrity.
- Reviewing API design.
- Reviewing code quality and maintainability.
- Reviewing test coverage.
- Reviewing documentation.
- Making final decisions regarding code and implementation.
- Being able to explain and defend the implementation during the review.

AI tools were used as engineering assistants and accelerators where appropriate. AI was not treated as the final authority for correctness, security, architecture, or production readiness.

---

## 3. AI-Assisted Development Declaration

AI assistance was used during the development of this assignment.

The purpose of using AI was to accelerate engineering activities while maintaining human ownership and review.

AI assistance may have been used for activities including:

- Requirement analysis.
- Requirement decomposition.
- Architecture brainstorming.
- Database design brainstorming.
- API design.
- Spring Boot implementation assistance.
- Java code generation or suggestions.
- Unit-test generation.
- Mockito test suggestions.
- Debugging assistance.
- Refactoring suggestions.
- Code-quality suggestions.
- Documentation generation.
- README/documentation assistance.
- Reviewing implementation approaches.
- Identifying potential edge cases.
- Reviewing security considerations.
- Preparing implementation checklists.

All AI-generated or AI-assisted output was subject to engineering review before being accepted into the implementation.

---

## 4. Engineer Review and Approval

AI-generated suggestions were not automatically accepted.

For each significant AI-assisted change, I reviewed the proposed solution and considered:

1. Whether it satisfies the requirement.
2. Whether the implementation is technically correct.
3. Whether the implementation is maintainable.
4. Whether it introduces security risks.
5. Whether it introduces data-integrity risks.
6. Whether it handles failure scenarios correctly.
7. Whether it is consistent with the overall architecture.
8. Whether appropriate tests exist.
9. Whether the implementation can be explained and defended.

Where AI-generated output was incomplete, incorrect, unnecessary, or inconsistent with the requirements, I modified, rejected, or replaced it.

---

# 5. Requirement Understanding

The assignment requires development of a tamper-evident audit log service.

The core system records an append-only history of events and provides mechanisms to detect unauthorized modification or deletion of historical records.

The implementation is based on the following major capabilities:

- Audit event creation.
- Append-only audit records.
- Audit event querying.
- Hash-chain tamper evidence.
- Chain verification.
- Retention handling.
- Structured redaction.
- Bulk export.
- Compliance reporting analysis.
- Automated testing.
- Quality validation.
- Security controls.
- Documentation.
- AI-assisted engineering traceability.

---

# 6. Scenario A — Core Audit Log Service

## Objective

Build the core tamper-evident audit log service.

## Functional Requirements

The write API supports audit events containing:

- `eventType`
- `actorId`
- `resourceType`
- `resourceId`
- `payload`
- `timestamp`

The service does not expose update or delete operations for audit records.

## Query API

The query API supports filtering by:

- `actorId`
- `resourceType`
- `resourceId`
- `eventType`
- Time range (`from` / `to`)

Pagination is supported for large result sets.

## Tamper Evidence

Each stored audit record contains:

- A hash of its own content.
- A hash/reference to the immediately preceding record.
- A defined genesis value for the first record.

The resulting records form a hash chain.

## Verification

The service exposes:

```text
GET /audit/verify