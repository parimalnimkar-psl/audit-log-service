CREATE TABLE IF NOT EXISTS audit_events (
  id BIGSERIAL PRIMARY KEY,
  chain_sequence BIGINT NOT NULL UNIQUE,
  event_type VARCHAR(100) NOT NULL,
  actor_id VARCHAR(150) NOT NULL,
  resource_type VARCHAR(100) NOT NULL,
  resource_id VARCHAR(150) NOT NULL,
  payload TEXT NOT NULL,
  event_timestamp TIMESTAMPTZ NOT NULL,
  previous_hash VARCHAR(64) NOT NULL,
  content_hash VARCHAR(64) NOT NULL,
  hash_algorithm VARCHAR(30) NOT NULL DEFAULT 'SHA-256',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  redaction_metadata TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_actor_time ON audit_events(actor_id, event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_resource_time ON audit_events(resource_type, resource_id, event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_event_type_time ON audit_events(event_type, event_timestamp);
CREATE SEQUENCE IF NOT EXISTS audit_chain_sequence START WITH 1 INCREMENT BY 1;
