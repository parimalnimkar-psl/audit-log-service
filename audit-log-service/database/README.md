# Database Details

## PostgreSQL database
Recommended database: `audit_log_db` on port `5432`.

1. Execute `postgresql/01-create-database.sql` as a PostgreSQL administrator.
2. Execute `postgresql/02-schema.sql` against `audit_log_db`, or allow Flyway under the `postgres` Spring profile to create the schema.
3. Configure `DB_USERNAME` and `DB_PASSWORD`.

## Main table: audit_events
- `id`: primary key.
- `chain_sequence`: unique, strictly ordered chain position.
- `event_type`, `actor_id`, `resource_type`, `resource_id`: searchable event metadata.
- `payload`: text JSON payload in the portable starter schema. PostgreSQL can be upgraded to JSONB with corresponding Hibernate mapping.
- `event_timestamp`: UTC timestamp.
- `previous_hash`: preceding event hash or genesis value.
- `content_hash`: SHA-256 of deterministic event content.
- `status`: ACTIVE, ARCHIVED or REDACTED.
- `redaction_metadata`: optional record of redaction operation.

Recommended indexes: actor/time, resource type/id/time and event type/time.
