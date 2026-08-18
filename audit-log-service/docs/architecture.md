# Architecture

`Client -> Spring Security/JWT -> REST Controller -> Service -> Hash/Verification Domain -> JPA Repository -> Database`

Modules: API, security, application service, domain hashing/verification, persistence and infrastructure.

Writes allocate a strictly increasing chain sequence. The current implementation uses a database sequence and transaction boundary. For high-contention production deployments, serialize access to a dedicated chain-head row or equivalent locking strategy.

Hash input is deterministic: `version|sequence|eventType|actorId|resourceType|resourceId|payload|timestamp`. SHA-256 output is hex encoded.
