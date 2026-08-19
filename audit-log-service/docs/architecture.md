# Architecture

`Client -> Spring Security/OAuth2 resource server -> REST Controller -> Service -> Hash/Verification Domain -> JPA Repository -> Database`

Modules: API, security, application service, domain hashing/verification, persistence and infrastructure.

Writes allocate a strictly increasing chain sequence from `audit_chain_sequence` and serialize append construction with a process lock. A multi-instance production deployment must additionally use a database-backed chain-head lock or single-writer deployment; the application does not claim distributed append serialization.

Hash input is deterministic: `version|sequence|eventType|actorId|resourceType|resourceId|payload|timestamp`. SHA-256 output is hex encoded.
