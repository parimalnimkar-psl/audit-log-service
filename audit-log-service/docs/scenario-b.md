# Scenario B — Retention, Redaction and Export

## Retention/archive
The `status` field supports ACTIVE, ARCHIVED and REDACTED. Status is intentionally excluded from the immutable event-content hash so operational archival state can change without rewriting historical chain evidence. A production retention job should archive according to the configured retention period and preserve the event's chain metadata.

## Redaction
Replacing an already-hashed payload is not safe because it changes the evidence and invalidates downstream links. A production-ready implementation should use a cryptographic commitment/envelope or encrypted payload with cryptographic erasure. This starter documents that design rather than falsely claiming plaintext deletion preserves the original hash.

## Export
A production export should include selected records plus chain sequence, previous hash, content hash, algorithm, genesis information and enough boundary evidence to verify the subset.
