# Scenario B — Retention, Redaction and Export

## Retention/archive
The `status` field supports ACTIVE, ARCHIVED and REDACTED. Status is intentionally excluded from the immutable event-content hash so operational archival state can change without rewriting historical chain evidence. The current service stores the configured retention period but does not run an archive scheduler; event-type retention rules and deletion/archival authority must be approved before implementation.

## Redaction
Replacing an already-hashed payload is not safe because it changes the evidence and invalidates downstream links. A production-ready implementation should use a cryptographic commitment/envelope or encrypted payload with cryptographic erasure. This starter documents that design rather than falsely claiming plaintext deletion preserves the original hash.

## Export
`GET /audit/export` is implemented for `AUDIT_EXPORTER` and `AUDIT_ADMIN`. It returns selected records plus chain sequence, previous hash, content hash, algorithm, genesis information, record count, and export timestamp. Compliance should confirm the final export format and boundary-evidence requirements before production adoption.
